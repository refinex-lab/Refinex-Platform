package cn.refinex.limiter.autoconfigure;

import com.alibaba.csp.sentinel.adapter.spring.webflux.callback.WebFluxCallbackManager;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Sentinel WebFlux 适配配置
 * <p>
 * 修复 Sentinel 在 Spring Cloud Gateway / WebFlux 环境下的限流异常处理问题。
 * 参考 Issue: <a href="https://github.com/alibaba/Sentinel/issues/3298">...</a>
 *
 * @author refinex
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(WebFluxCallbackManager.class)
public class SentinelWebFluxAutoConfiguration {

    /**
     * 初始化网关限流后的处理逻辑
     */
    @PostConstruct
    public void initGatewayBlockHandler() {
        // 自定义限流后的返回响应
        WebFluxCallbackManager.setBlockHandler((exchange, t) ->
                ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just("{\"code\":\"429\", \"message\":\"请求过于频繁，请稍后再试\"}"), String.class));
    }
}
