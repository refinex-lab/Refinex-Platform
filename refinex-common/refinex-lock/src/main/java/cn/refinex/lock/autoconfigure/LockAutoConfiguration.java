package cn.refinex.lock.autoconfigure;

import cn.refinex.lock.aspect.DistributedLockAspect;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 分布式锁自动配置
 *
 * @author refinex
 */
@AutoConfiguration
@ConditionalOnClass(RedissonClient.class) // 如果类路径下存在 RedissonClient 类则进行自动配置
public class LockAutoConfiguration {

    /**
     * 分布式锁切面
     *
     * @param redissonClient Redisson 客户端
     * @return 分布式锁切面
     */
    @Bean
    @ConditionalOnMissingBean
    public DistributedLockAspect distributedLockAspect(RedissonClient redissonClient) {
        return new DistributedLockAspect(redissonClient);
    }
}
