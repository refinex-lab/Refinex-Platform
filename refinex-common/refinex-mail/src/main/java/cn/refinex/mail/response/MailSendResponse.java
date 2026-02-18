package cn.refinex.mail.response;

import cn.refinex.base.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 邮件发送响应
 *
 * @author refinex
 */
@Getter
@Setter
@ToString(callSuper = true)
public class MailSendResponse extends BaseResponse {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 邮件消息ID
     */
    private String messageId;

    /**
     * 成功响应
     */
    public static MailSendResponse success(String messageId) {
        MailSendResponse response = new MailSendResponse();
        response.setSuccess(true);
        response.setMessageId(messageId);
        return response;
    }

    /**
     * 失败响应
     */
    public static MailSendResponse failure(String code, String message) {
        MailSendResponse response = new MailSendResponse();
        response.setSuccess(false);
        response.setResponseCode(code);
        response.setResponseMessage(message);
        return response;
    }
}
