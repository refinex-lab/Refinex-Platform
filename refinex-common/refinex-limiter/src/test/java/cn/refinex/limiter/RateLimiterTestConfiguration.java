package cn.refinex.limiter;

import cn.refinex.limiter.support.SlidingWindowRateLimiter;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Rate Limiter Test
 *
 * @author refinex
 */
@TestConfiguration
public class RateLimiterTestConfiguration {

    @Value("${refinex.redis.url:127.0.0.1:6379}")
    private String redisUrl;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 简单的单机配置用于测试，生产环境请使用 Cluster 或 Sentinel
        String prefix = redisUrl.startsWith("redis://") ? "" : "redis://";
        config.useSingleServer().setAddress(prefix + redisUrl);
        return Redisson.create(config);
    }

    @Bean
    public SlidingWindowRateLimiter slidingWindowRateLimiter(RedissonClient redissonClient) {
        return new SlidingWindowRateLimiter(redissonClient);
    }
}
