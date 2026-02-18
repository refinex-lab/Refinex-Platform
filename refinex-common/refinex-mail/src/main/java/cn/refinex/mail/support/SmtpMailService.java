package cn.refinex.mail.support;

import cn.refinex.base.response.code.ResponseCode;
import cn.refinex.mail.api.MailService;
import cn.refinex.mail.autoconfigure.MailProperties;
import cn.refinex.mail.response.MailSendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

/**
 * 基于 SMTP 的邮件服务实现
 *
 * @author refinex
 */
@Slf4j
@RequiredArgsConstructor
public class SmtpMailService implements MailService {

    private final JavaMailSender mailSender;
    private final MailProperties properties;

    @Override
    public MailSendResponse sendHtml(String to, String subject, String htmlContent) {
        if (StringUtils.isBlank(to) || StringUtils.isBlank(subject) || StringUtils.isBlank(htmlContent)) {
            return MailSendResponse.failure(ResponseCode.ILLEGAL_ARGUMENT.name(), "邮件参数不完整");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(buildFromAddress());

            mailSender.send(message);
            return MailSendResponse.success(message.getMessageID());
        } catch (Exception ex) {
            log.error("Failed to send mail to: {}", to, ex);
            String errMsg = StringUtils.substring(ex.getMessage(), 0, 200);
            return MailSendResponse.failure(ResponseCode.SYSTEM_ERROR.name(), "邮件发送异常: " + errMsg);
        }
    }

    private InternetAddress buildFromAddress() throws Exception {
        String fromAddress = properties.getFromAddress();
        String fromName = properties.getFromName();
        if (StringUtils.isBlank(fromAddress)) {
            throw new IllegalArgumentException("refinex.mail.from-address is required when mail enabled");
        }
        if (StringUtils.isBlank(fromName)) {
            return new InternetAddress(fromAddress);
        }
        return new InternetAddress(fromAddress, fromName, StandardCharsets.UTF_8.name());
    }
}
