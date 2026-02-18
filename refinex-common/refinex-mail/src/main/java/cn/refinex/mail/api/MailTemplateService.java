package cn.refinex.mail.api;

import cn.refinex.mail.response.MailSendResponse;

import java.util.Map;

/**
 * 模板邮件服务
 *
 * @author refinex
 */
public interface MailTemplateService {

    /**
     * 按模板发送邮件
     *
     * @param to           收件人邮箱
     * @param subject      邮件主题
     * @param templateName 模板名（不含前后缀）
     * @param variables    模板变量
     * @return 发送结果
     */
    MailSendResponse sendTemplate(String to, String subject, String templateName, Map<String, Object> variables);
}
