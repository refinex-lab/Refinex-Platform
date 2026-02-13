package cn.refinex.sms.support;

import cn.refinex.lock.annotation.DistributedLock;
import cn.refinex.sms.api.SmsService;
import cn.refinex.sms.response.SmsSendResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Mock 短信服务 (开发/测试环境使用)
 *
 * @author refinex
 */
@Slf4j
public class MockSmsService implements SmsService {

    /**
     * 发送短信验证码
     *
     * @param phoneNumber 接收手机号
     * @param code        验证码内容
     * @return 发送结果
     */
    @DistributedLock(
            scene = "SEND_SMS",
            keyExpression = "#phoneNumber",
            waitTime = 0, // 拿不到锁直接失败，防刷
            leaseTime = 60000, // 锁持有 60s，即同一手机号 60s 内只能发送一次（业务防刷逻辑）
            errorMessage = "短信发送过于频繁，请1分钟后再试"
    )
    @Override
    public SmsSendResponse sendMsg(String phoneNumber, String code) {
        log.info("[Mock SMS] Sent code: {} to phone: {}", code, phoneNumber);
        return SmsSendResponse.success();
    }
}
