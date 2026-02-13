package cn.refinex.sms.api;

import cn.refinex.sms.response.SmsSendResponse;

/**
 * 短信服务接口
 *
 * @author refinex
 */
public interface SmsService {

    /**
     * 发送短信验证码
     *
     * @param phoneNumber 接收手机号
     * @param code        验证码内容
     * @return 发送结果
     */
    SmsSendResponse sendMsg(String phoneNumber, String code);
}
