package cn.refinex.base.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Refinex 加密配置属性
 * <p>
 * 通过 {@code refinex.crypto} 前缀绑定，敏感值建议走环境变量注入。
 *
 * @author refinex
 */
@Data
@ConfigurationProperties(prefix = "refinex.crypto")
public class RefinexCryptoProperties {

    /**
     * AES 密钥（16/24/32 字节）
     */
    private String aesKey;
}
