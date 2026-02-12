package cn.refinex.lock.annotation;

import cn.refinex.lock.constant.DistributedLockConstant;

import java.lang.annotation.*;

/**
 * 分布式锁注解
 * <p>
 * 基于 Redisson 实现，支持 SpEL 表达式 key 解析、自动续期（WatchDog）、等待超时控制。
 *
 * @author refinex
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 锁的业务场景前缀 (Namespace)
     * <p>
     * 例如: "ORDER_PAY", "USER_REGISTER"
     * 最终 Key 格式: {scene}:{key}
     */
    String scene();

    /**
     * 固定 Key 后缀
     * <p>
     * 如果 key() 和 keyExpression() 都未设置，则抛出异常。
     */
    String key() default DistributedLockConstant.NONE_KEY;

    /**
     * SpEL 表达式 Key
     * <p>
     * 示例: "#user.id", "#req.orderNo"
     * 优先级低于 key()，当 key() 为 NONE 时生效。
     */
    String keyExpression() default DistributedLockConstant.NONE_KEY;

    /**
     * 锁的持有时间 (Lease Time)，单位：毫秒
     * <p>
     * 默认值 -1: 启用 WatchDog 自动续期机制（推荐，防止业务执行时间过长导致锁提前释放）。
     * 大于 0: 锁在指定时间后强制释放（慎用，需确保业务能在该时间内完成）。
     */
    long leaseTime() default DistributedLockConstant.DEFAULT_LEASE_TIME;

    /**
     * 获取锁的等待时间 (Wait Time)，单位：毫秒
     * <p>
     * 默认值 -1: 尝试获取一次，失败立即抛出异常（非阻塞模式）。
     * 大于 0: 尝试获取锁，最多等待指定时间，超时后抛出异常（阻塞等待模式）。
     */
    long waitTime() default DistributedLockConstant.DEFAULT_WAIT_TIME;

    /**
     * 锁失败时的提示信息
     */
    String errorMessage() default "系统繁忙，请稍后再试";
}
