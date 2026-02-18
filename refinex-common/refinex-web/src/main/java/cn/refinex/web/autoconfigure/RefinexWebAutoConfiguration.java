package cn.refinex.web.autoconfigure;

import cn.refinex.web.filter.TokenFilter;
import cn.refinex.web.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

/**
 * Web 层自动配置类
 * <p>
 * 负责注册全局异常处理器、安全过滤器等 Web 基础组件。
 *
 * @author refinex
 */
@AutoConfiguration
@ConditionalOnWebApplication // 仅在 Web 环境下生效
@EnableConfigurationProperties(RefinexWebProperties.class)
public class RefinexWebAutoConfiguration implements WebMvcConfigurer {

    /**
     * 注册全局异常处理器
     * <p>
     * 使用 @ConditionalOnMissingBean 允许业务服务通过自定义 Bean 覆盖默认异常处理逻辑。
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    /**
     * 注册 Token 校验过滤器
     *
     * @param tokenFilter       Spring 管理的 TokenFilter Bean (确保 @Value 注入生效)
     * @param webProperties     配置属性
     * @return Filter 注册对象
     */
    @Bean
    @ConditionalOnMissingBean(name = "tokenFilterRegistration")
    @ConditionalOnProperty(prefix = "refinex.web", name = "enabled", havingValue = "true") // 默认关闭，按需启用
    public FilterRegistrationBean<TokenFilter> tokenFilterRegistration(TokenFilter tokenFilter, RefinexWebProperties webProperties) {
        FilterRegistrationBean<TokenFilter> registrationBean = new FilterRegistrationBean<>();

        // 重点：这里传入的是 Spring 容器中的 Bean，而不是 new TokenFilter()
        registrationBean.setFilter(tokenFilter);

        // 设置拦截路径 (从配置文件读取，而非硬编码)
        if (webProperties.getAuthUrls() != null && !webProperties.getAuthUrls().isEmpty()) {
            registrationBean.setUrlPatterns(webProperties.getAuthUrls());
        } else {
            // 如果没配置，默认不拦截任何路径，或者拦截所有 (视策略而定，这里选择安全兜底不拦截)
            registrationBean.setUrlPatterns(Collections.emptyList());
        }

        registrationBean.setOrder(webProperties.getFilterOrder());
        registrationBean.setName("refinexTokenFilter");

        return registrationBean;
    }
}
