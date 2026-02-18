package cn.refinex.mail.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 邮件服务配置
 *
 * @author refinex
 */
@Data
@ConfigurationProperties(prefix = "refinex.mail")
public class MailProperties {

    /**
     * 是否启用真实邮件发送（false 时走 Mock 服务）
     */
    private boolean enabled = false;

    /**
     * 发件邮箱地址
     */
    private String fromAddress;

    /**
     * 发件人名称
     */
    private String fromName = "Refinex";

    /**
     * 模板前缀（classpath）
     */
    private String templatePrefix = "mail/templates/";

    /**
     * 模板后缀
     */
    private String templateSuffix = ".html";

    /**
     * 是否缓存模板
     */
    private boolean templateCache = true;
}
