package cn.refinex.ai.infrastructure.config;

import cn.refinex.web.autoconfigure.RefinexWebProperties;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 服务 WebFlux 配置
 * <p>
 * 注册 WebFlux 版的 Token 过滤器和全局异常处理器，
 * 替代 refinex-web 中基于 Servlet 的 {@code TokenFilter} 和 {@code GlobalExceptionHandler}。
 *
 * @author refinex
 */
@Configuration
@EnableConfigurationProperties(RefinexWebProperties.class)
public class AiWebFluxConfiguration {

    /**
     * WebFlux 版全局异常处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public ReactiveGlobalExceptionHandler reactiveGlobalExceptionHandler() {
        return new ReactiveGlobalExceptionHandler();
    }

    /**
     * WebFlux 版 Token 校验过滤器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "refinex.web", name = "enabled", havingValue = "true")
    public ReactiveTokenFilter reactiveTokenFilter(RedissonClient redissonClient, RefinexWebProperties webProperties) {
        return new ReactiveTokenFilter(redissonClient, webProperties);
    }
}
