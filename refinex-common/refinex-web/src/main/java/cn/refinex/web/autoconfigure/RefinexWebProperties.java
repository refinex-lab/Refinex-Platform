package cn.refinex.web.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Web 模块配置属性
 * <p>
 * 对应 application.yml 中的 refinex.web 配置项
 *
 * @author refinex
 */
@Data
@ConfigurationProperties(prefix = "refinex.web")
public class RefinexWebProperties {

    /**
     * 是否启用 TokenFilter
     * <p>
     * 默认关闭，避免与 Sa-Token 登录态鉴权混用。
     */
    private boolean enabled = false;

    /**
     * 需要进行 Token 校验的 URL 路径列表
     * <p>
     * 示例：
     * - /trade/buy
     * - /user/info
     */
    private List<String> authUrls = new ArrayList<>();

    /**
     * 免鉴权 URL 路径列表（支持 Ant 风格通配）
     * <p>
     * 示例：
     * - /auth/login
     * - /auth/sms/send
     * - /auth/email/send
     */
    private List<String> excludeUrls = new ArrayList<>();

    /**
     * Token 过滤器的优先级 (默认 10)
     */
    private int filterOrder = 10;
}
