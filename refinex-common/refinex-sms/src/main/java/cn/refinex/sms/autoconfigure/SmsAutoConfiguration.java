package cn.refinex.sms.autoconfigure;

import cn.refinex.sms.api.SmsService;
import cn.refinex.sms.support.GuoYangSmsService;
import cn.refinex.sms.support.MockSmsService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 短信服务自动配置
 *
 * @author refinex
 */
@AutoConfiguration
@EnableConfigurationProperties(SmsProperties.class)
public class SmsAutoConfiguration {

    /**
     * 当 refinex.sms.enabled = true 时，加载真实短信服务
     */
    @Bean
    @ConditionalOnMissingBean(SmsService.class)
    @ConditionalOnProperty(prefix = "refinex.sms", name = "enabled", havingValue = "true")
    public SmsService guoYangSmsService(SmsProperties properties) {
        return new GuoYangSmsService(properties);
    }

    /**
     * 兜底：未开启时使用 Mock 服务
     */
    @Bean
    @ConditionalOnMissingBean(SmsService.class)
    public SmsService mockSmsService() {
        return new MockSmsService();
    }
}
