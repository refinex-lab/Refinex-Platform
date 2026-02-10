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
     * 需要进行 Token 校验的 URL 路径列表
     * <p>
     * 示例：
     * - /trade/buy
     * - /user/info
     */
    private List<String> authUrls = new ArrayList<>();

    /**
     * Token 过滤器的优先级 (默认 10)
     */
    private int filterOrder = 10;
}
