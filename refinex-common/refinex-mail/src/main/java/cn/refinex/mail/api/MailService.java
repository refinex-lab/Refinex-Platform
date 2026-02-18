package cn.refinex.mail.api;

import cn.refinex.mail.response.MailSendResponse;

/**
 * 邮件发送服务
 *
 * @author refinex
 */
public interface MailService {

    /**
     * 发送 HTML 邮件
     *
     * @param to          收件人邮箱
     * @param subject     邮件主题
     * @param htmlContent HTML 正文
     * @return 发送结果
     */
    MailSendResponse sendHtml(String to, String subject, String htmlContent);
}
