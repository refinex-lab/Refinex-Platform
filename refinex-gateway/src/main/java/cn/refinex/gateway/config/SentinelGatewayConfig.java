package cn.refinex.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Sentinel 网关限流配置
 *
 * @author refinex
 */
@Configuration
public class SentinelGatewayConfig {

    /**
     * 自定义限流异常返回
     */
    @PostConstruct
    public void initGatewayBlockHandler() {
        GatewayCallbackManager.setBlockHandler((exchange, t) ->
                ServerResponse
                        // 设置状态码: 429 Too Many Requests
                        .status(HttpStatus.TOO_MANY_REQUESTS)
                        // 设置内容类型: application/json
                        .contentType(MediaType.APPLICATION_JSON)
                        // 设置响应体
                        .body(BodyInserters.fromValue("{\"code\": 429, \"message\": \"当前访问人数过多，请稍后再试\"}"))
        );
    }
}
