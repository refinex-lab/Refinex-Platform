package cn.refinex.sms.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 短信服务配置属性
 *
 * @author refinex
 */
@Data
@ConfigurationProperties(prefix = "refinex.sms")
public class SmsProperties {

    /**
     * 是否启用真实短信发送 (默认 false，使用 Mock)
     */
    private boolean enabled = false;

    /**
     * API 域名 (例如: https://gyytz.market.alicloudapi.com)
     */
    private String host;

    /**
     * API 路径 (例如: /sms/smsSend)
     */
    private String path;

    /**
     * 阿里云云市场 AppCode
     */
    private String appcode;

    /**
     * 短信签名 ID (需在控制台申请)
     */
    private String smsSignId;

    /**
     * 短信模板 ID (需在控制台申请)
     */
    private String templateId;
}
