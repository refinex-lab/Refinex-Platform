package cn.refinex.sms.response;

import cn.refinex.base.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 短信发送响应结果
 *
 * @author refinex
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SmsSendResponse extends BaseResponse {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 业务流水号 (第三方返回的短信 ID，可用于回执查询)
     */
    private String bizId;

    /**
     * 成功
     *
     * @return 短信发送响应结果
     */
    public static SmsSendResponse success() {
        SmsSendResponse response = new SmsSendResponse();
        response.setSuccess(true);
        return response;
    }

    /**
     * 失败
     *
     * @param code    响应码
     * @param message 响应消息
     * @return 短信发送响应结果
     */
    public static SmsSendResponse failure(String code, String message) {
        SmsSendResponse response = new SmsSendResponse();
        response.setSuccess(false);
        response.setResponseCode(code);
        response.setResponseMessage(message);
        return response;
    }
}
