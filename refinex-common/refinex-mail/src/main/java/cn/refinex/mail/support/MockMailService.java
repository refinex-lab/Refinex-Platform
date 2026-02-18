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

    @Override
    public MailSendResponse sendHtml(String to, String subject, String htmlContent) {
        log.info("[Mock Mail] to={}, subject={}", to, subject);
        return MailSendResponse.success("MOCK-MAIL");
    }
}
