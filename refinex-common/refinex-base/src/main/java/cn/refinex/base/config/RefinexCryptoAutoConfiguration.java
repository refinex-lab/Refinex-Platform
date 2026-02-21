package cn.refinex.base.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Refinex 加密自动配置
 * <p>
 * 注册 {@link RefinexCryptoProperties}，使各服务可通过注入该属性获取 AES 密钥。
 *
 * @author refinex
 */
@AutoConfiguration
@EnableConfigurationProperties(RefinexCryptoProperties.class)
public class RefinexCryptoAutoConfiguration {
}
