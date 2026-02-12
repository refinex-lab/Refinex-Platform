package cn.refinex.limiter.autoconfigure;

import cn.refinex.limiter.support.SlidingWindowRateLimiter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 限流组件自动配置
 *
 * @author refinex
 */
@AutoConfiguration
public class LimiterAutoConfiguration {

    /**
     * 提供滑动窗口限流器
     *
     * @param redissonClient Redisson 客户端
     * @return 滑动窗口限流器
     */
    @Bean
    @ConditionalOnBean(RedissonClient.class)
    @ConditionalOnMissingBean
    public SlidingWindowRateLimiter slidingWindowRateLimiter(RedissonClient redissonClient) {
        return new SlidingWindowRateLimiter(redissonClient);
    }
}
