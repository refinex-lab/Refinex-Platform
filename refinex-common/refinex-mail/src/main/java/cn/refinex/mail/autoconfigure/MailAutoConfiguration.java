package cn.refinex.mail.autoconfigure;

import cn.refinex.mail.api.MailService;
import cn.refinex.mail.api.MailTemplateService;
import cn.refinex.mail.support.MockMailService;
import cn.refinex.mail.support.SmtpMailService;
import cn.refinex.mail.support.ThymeleafMailTemplateService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * 邮件服务自动配置
 *
 * @author refinex
 */
@AutoConfiguration(after = MailSenderAutoConfiguration.class)
@EnableConfigurationProperties(MailProperties.class)
public class MailAutoConfiguration {

    /**
     * 真实 SMTP 邮件服务
     */
    @Bean
    @ConditionalOnBean(JavaMailSender.class)
    @ConditionalOnMissingBean(MailService.class)
    @ConditionalOnProperty(prefix = "refinex.mail", name = "enabled", havingValue = "true")
    public MailService smtpMailService(JavaMailSender mailSender, MailProperties properties) {
        return new SmtpMailService(mailSender, properties);
    }

    /**
     * 兜底 Mock 邮件服务
     */
    @Bean
    @ConditionalOnMissingBean(MailService.class)
    public MailService mockMailService() {
        return new MockMailService();
    }

    /**
     * 模板引擎
     */
    @Bean
    @ConditionalOnMissingBean(TemplateEngine.class)
    public TemplateEngine mailTemplateEngine(MailProperties properties) {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix(properties.getTemplatePrefix());
        resolver.setSuffix(properties.getTemplateSuffix());
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(properties.isTemplateCache());
        resolver.setCheckExistence(true);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        return templateEngine;
    }

    /**
     * 模板邮件服务
     */
    @Bean
    @ConditionalOnMissingBean(MailTemplateService.class)
    public MailTemplateService mailTemplateService(MailService mailService, TemplateEngine templateEngine) {
        return new ThymeleafMailTemplateService(mailService, templateEngine);
    }
}
