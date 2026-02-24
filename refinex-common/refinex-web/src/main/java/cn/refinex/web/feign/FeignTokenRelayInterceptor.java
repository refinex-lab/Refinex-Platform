package cn.refinex.web.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign Token 透传拦截器
 * <p>
 * 从当前 HTTP 请求上下文提取 Sa-Token 并注入到 Feign 请求头，
 * 实现服务间调用的 Token 透传。
 *
 * @author refinex
 */
@Component
@ConditionalOnClass(RequestInterceptor.class)
public class FeignTokenRelayInterceptor implements RequestInterceptor {

    @Value("${sa-token.token-name:Refinex-Token}")
    private String tokenName;

    /**
     * 将当前请求的 Token 透传到 Feign 请求头
     *
     * @param template Feign 请求模板
     */
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            String token = attrs.getRequest().getHeader(tokenName);
            if (token != null && !token.isBlank()) {
                template.header(tokenName, token);
            }
        }
    }
}
