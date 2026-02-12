package cn.refinex.stream.autoconfigure;

import cn.refinex.stream.producer.StreamProducer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

/**
 * Stream 模块自动配置
 *
 * @author refinex
 */
@AutoConfiguration
@ConditionalOnClass(StreamBridge.class)
public class StreamAutoConfiguration {

    /**
     * 注册 StreamProducer
     * <p>
     * 默认开启。可以通过设置 refinex.stream.enabled=false 关闭 (例如在不依赖 MQ 的纯计算节点)
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "refinex.stream", name = "enabled", havingValue = "true", matchIfMissing = true)
    public StreamProducer streamProducer(StreamBridge streamBridge) {
        return new StreamProducer(streamBridge);
    }
}
