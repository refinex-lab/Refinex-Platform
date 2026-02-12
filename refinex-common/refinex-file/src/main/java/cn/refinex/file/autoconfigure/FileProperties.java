package cn.refinex.file.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件服务配置属性
 *
 * @author refinex
 */
@Data
@ConfigurationProperties(prefix = "refinex.file.oss")
public class FileProperties {

    /**
     * 是否启用 OSS (默认 false，使用 Mock)
     */
    private boolean enabled = false;

    /**
     * Endpoint (地域节点)
     * 例如: https://oss-cn-hangzhou.aliyuncs.com
     */
    private String endpoint;

    /**
     * Bucket 名称
     */
    private String bucket;

    /**
     * AccessKey ID
     */
    private String accessKey;

    /**
     * AccessKey Secret
     */
    private String accessSecret;

    /**
     * 访问域名 (CDN 自定义域名)
     * 可选。如果配置了 CDN，返回的 URL 将使用此域名。
     * 例如: https://cdn.example.com
     */
    private String domain;
}
