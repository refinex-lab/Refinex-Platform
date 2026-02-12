package cn.refinex.limiter.support;

import cn.refinex.limiter.api.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

/**
 * 基于 Redisson 的滑动窗口限流实现
 * <p>
 * 适用于分布式环境下的精确限流。
 * 注意：Redisson 的 RRateLimiter 配置是持久化的。如果代码中修改了 limit/windowSize，
 * 但 Redis 中 key 已存在，旧配置不会自动更新。建议在 key 中包含版本号或定期过期。
 *
 * @author refinex
 */
@Slf4j
public class SlidingWindowRateLimiter implements RateLimiter {

    /**
     * Redisson 客户端
     */
    private final RedissonClient redissonClient;

    /**
     * 限流 Key 前缀，防止与其他业务 Key 冲突
     */
    private static final String LIMIT_KEY_PREFIX = "refinex:limiter:";

    /**
     * 构造函数
     *
     * @param redissonClient Redisson 客户端
     */
    public SlidingWindowRateLimiter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 尝试获取许可（非阻塞）
     *
     * @param key        限流资源 Key (如: "order_create_ip:127.0.0.1")
     * @param limit      限流阈值 (如: 10)
     * @param windowSize 窗口大小，单位：秒 (如: 1)
     * @return true 表示通过，false 表示被限流
     */
    @Override
    public boolean tryAcquire(String key, int limit, int windowSize) {
        // 构造完整的 Redis Key
        String fullKey = LIMIT_KEY_PREFIX + key;

        RRateLimiter rateLimiter = redissonClient.getRateLimiter(fullKey);

        // 初始化限流规则
        // trySetRate: 仅当配置不存在时设置。返回 true 表示设置成功，false 表示已存在。
        // RateType.OVERALL: 全局限流（所有客户端共享）
        rateLimiter.trySetRate(RateType.OVERALL, limit, windowSize, RateIntervalUnit.SECONDS);

        // 尝试获取 1 个许可
        boolean acquired = rateLimiter.tryAcquire(1);

        if (!acquired) {
            log.debug("Rate limit exceeded for key: {}", key);
        }

        return acquired;
    }
}
