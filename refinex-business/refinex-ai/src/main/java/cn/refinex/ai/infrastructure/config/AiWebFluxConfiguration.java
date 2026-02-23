package cn.refinex.ai.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 服务 WebFlux 配置
 * <p>
 * 注册 WebFlux 版的 Token 过滤器和全局异常处理器，
 * 替代 refinex-web 中基于 Servlet 的对应组件。
 *
 * @author refinex
 */
@Configuration
public class AiWebFluxConfiguration {

    /**
     * WebFlux 版全局异常处理器
     *
     * @return ReactiveGlobalExceptionHandler 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public ReactiveGlobalExceptionHandler reactiveGlobalExceptionHandler() {
        return new ReactiveGlobalExceptionHandler();
    }

    /**
     * WebFlux 版登录用户解析过滤器
     *
     * @return ReactiveTokenFilter 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public ReactiveTokenFilter reactiveTokenFilter() {
        return new ReactiveTokenFilter();
    }
}
