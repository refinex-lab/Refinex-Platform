package cn.refinex.mail.support;

import cn.refinex.mail.api.MailService;
import cn.refinex.mail.response.MailSendResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Mock 邮件服务（开发/测试环境）
 *
 * @author refinex
 */
@Slf4j
public class MockMailService implements MailService {

    /**
     * 发送 HTML 邮件
     *
     * @param to          收件人邮箱
     * @param subject     邮件主题
     * @param htmlContent HTML 正文
     * @return 发送结果
     */
    @Override
    public MailSendResponse sendHtml(String to, String subject, String htmlContent) {
        log.info("[Mock Mail] to={}, subject={}", to, subject);
        return MailSendResponse.success("MOCK-MAIL");
    }
}
