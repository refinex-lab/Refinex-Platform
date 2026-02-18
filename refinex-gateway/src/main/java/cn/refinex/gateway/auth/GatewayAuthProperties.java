package cn.refinex.gateway.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关鉴权配置
 *
 * @author refinex
 */
@Data
@ConfigurationProperties(prefix = "refinex.gateway.auth")
public class GatewayAuthProperties {

    /**
     * 登录校验白名单路径
     */
    private List<String> excludePaths = new ArrayList<>();
}
