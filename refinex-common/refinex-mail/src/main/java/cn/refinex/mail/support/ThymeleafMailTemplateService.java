package cn.refinex.mail.support;

import cn.refinex.mail.api.MailService;
import cn.refinex.mail.api.MailTemplateService;
import cn.refinex.mail.response.MailSendResponse;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

/**
 * Thymeleaf 模板邮件服务
 *
 * @author refinex
 */
@RequiredArgsConstructor
public class ThymeleafMailTemplateService implements MailTemplateService {

    private final MailService mailService;
    private final TemplateEngine templateEngine;

    /**
     * 按模板发送邮件
     *
     * @param to           收件人邮箱
     * @param subject      邮件主题
     * @param templateName 模板名（不含前后缀）
     * @param variables    模板变量
     * @return 发送结果
     */
    @Override
    public MailSendResponse sendTemplate(String to, String subject, String templateName, Map<String, Object> variables) {
        Context context = new Context(Locale.SIMPLIFIED_CHINESE);
        if (variables != null && !variables.isEmpty()) {
            context.setVariables(variables);
        }
        String html = templateEngine.process(templateName, context);
        return mailService.sendHtml(to, subject, html);
    }
}
