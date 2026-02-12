package cn.refinex.lock.aspect;

import cn.refinex.lock.annotation.DistributedLock;
import cn.refinex.lock.constant.DistributedLockConstant;
import cn.refinex.lock.exception.DistributedLockException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁切面实现
 *
 * @author refinex
 */
@Slf4j
@Aspect
@Order(Integer.MIN_VALUE + 10) // 优先级极高，确保在事务注解(@Transactional)之前生效
public class DistributedLockAspect {

    /**
     * Redisson 客户端
     */
    private final RedissonClient redissonClient;

    /**
     * SpEL 解析器 (线程安全，静态复用)
     */
    private static final SpelExpressionParser SPEL_PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    /**
     * 构造函数
     *
     * @param redissonClient Redisson 客户端
     */
    public DistributedLockAspect(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 环绕通知
     *
     * @param joinPoint 连接点
     * @param distributedLock 分布式锁注解
     * @return 结果
     * @throws Throwable 可能的异常
     */
    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        // 1. 解析锁 Key
        String businessKey = parseKey(distributedLock, joinPoint);
        String lockKey = distributedLock.scene() + ":" + businessKey;

        // 2. 获取锁对象
        RLock rLock = redissonClient.getLock(lockKey);

        long leaseTime = distributedLock.leaseTime();
        long waitTime = distributedLock.waitTime();
        boolean isLocked = false;

        try {
            // 3. 执行加锁逻辑
            if (waitTime > 0) {
                // 场景 A: 带等待时间的尝试加锁 (阻塞直到超时)
                if (leaseTime > 0) {
                    // 指定了租约时间 (WatchDog 不生效)
                    isLocked = rLock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
                } else {
                    // 未指定租约时间 (WatchDog 生效)
                    isLocked = rLock.tryLock(waitTime, TimeUnit.MILLISECONDS);
                }
            } else {
                // 场景 B: 不等待 (Fail-Fast) 或 默认阻塞
                // 如果 waitTime == -1，认为是 Fail-Fast (tryLock 立即返回)
                // 这里采用 Fail-Fast 策略，因为 waitTime 默认为 -1。
                if (leaseTime > 0) {
                    isLocked = rLock.tryLock(0, leaseTime, TimeUnit.MILLISECONDS);
                } else {
                    isLocked = rLock.tryLock(0, TimeUnit.MILLISECONDS);
                }
            }

            // 4. 加锁失败处理
            if (!isLocked) {
                log.warn("Failed to acquire lock: [{}], waitTime: {}ms", lockKey, waitTime);
                throw new DistributedLockException(distributedLock.errorMessage());
            }

            log.debug("Lock acquired: [{}], thread: {}", lockKey, Thread.currentThread().threadId());

            // 5. 执行业务逻辑
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DistributedLockException("Lock acquisition interrupted", e);
        } catch (Throwable e) {
            // 异常透传，不要包装成 Checked Exception
            throw e;
        } finally {
            // 6. 释放锁
            // 只有当前线程持有锁时才释放，防止释放了别人的锁 (Redisson 内部也做了判断，这里双重保险)
            if (isLocked && rLock.isHeldByCurrentThread()) {
                try {
                    rLock.unlock();
                    log.debug("Lock released: [{}]", lockKey);
                } catch (Exception e) {
                    log.error("Failed to release lock: [{}]", lockKey, e);
                }
            }
        }
    }

    /**
     * 解析 SpEL 表达式或直接获取 Key
     *
     * @param lock 分布式锁注解
     * @param joinPoint 连接点
     * @return 锁 Key
     */
    private String parseKey(DistributedLock lock, ProceedingJoinPoint joinPoint) {
        // 优先使用固定 Key
        if (!DistributedLockConstant.NONE_KEY.equals(lock.key())) {
            return lock.key();
        }

        // 使用 SpEL 表达式
        String expressionStr = lock.keyExpression();
        if (DistributedLockConstant.NONE_KEY.equals(expressionStr)) {
            throw new DistributedLockException("DistributedLock key and keyExpression cannot both be empty");
        }

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Object[] args = joinPoint.getArgs();

        // 解析上下文
        EvaluationContext context = new StandardEvaluationContext();
        String[] paramNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);

        if (paramNames != null && paramNames.length > 0) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        // 解析值
        try {
            Expression expression = SPEL_PARSER.parseExpression(expressionStr);
            Object value = expression.getValue(context);
            Assert.notNull(value, "Lock key cannot be null");
            return value.toString();
        } catch (Exception e) {
            log.error("SpEL expression parse failed: [{}]", expressionStr, e);
            throw new DistributedLockException("Invalid lock key expression");
        }
    }
}
