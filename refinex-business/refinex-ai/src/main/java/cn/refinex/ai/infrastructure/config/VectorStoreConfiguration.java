package cn.refinex.ai.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 向量存储配置类
 * <p>
 * 仅启用 Properties 绑定，VectorStore 实例由 VectorStoreFactory 懒创建。
 *
 * @author refinex
 */
@Configuration
@EnableConfigurationProperties(VectorStoreProperties.class)
public class VectorStoreConfiguration {
}
