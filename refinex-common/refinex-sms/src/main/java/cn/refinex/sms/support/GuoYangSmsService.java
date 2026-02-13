package cn.refinex.sms.support;

import cn.refinex.base.response.code.ResponseCode;
import cn.refinex.base.utils.RestClientUtils;
import cn.refinex.lock.annotation.DistributedLock;
import cn.refinex.sms.api.SmsService;
import cn.refinex.sms.autoconfigure.SmsProperties;
import cn.refinex.sms.response.SmsSendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * 国阳云短信发送服务实现
 *
 * @author refinex
 */
@Slf4j
@RequiredArgsConstructor
public class GuoYangSmsService implements SmsService {

    private final SmsProperties properties;

    /**
     * 发送短信验证码
     *
     * @param phoneNumber 接收手机号
     * @param code        验证码内容
     * @return 发送结果
     */
    @DistributedLock( // 分布式锁防刷：同一个手机号 60 秒内只能调用一次该方法
            scene = "SEND_SMS",
            keyExpression = "#phoneNumber",
            waitTime = 0, // 拿不到锁直接失败，防刷
            leaseTime = 60000, // 锁持有 60s，即同一手机号 60s 内只能发送一次（业务防刷逻辑）
            errorMessage = "短信发送过于频繁，请1分钟后再试"
    )
    @Override
    public SmsSendResponse sendMsg(String phoneNumber, String code) {
        // 1. 组装 Header
        Map<String, String> headers = Map.of(
                "Authorization", "APPCODE " + properties.getAppcode()
        );

        // 2. 组装 Query 参数
        Map<String, String> queries = new HashMap<>();
        queries.put("mobile", phoneNumber);
        // 短信变量，此处硬编码了 minute:5，如果有更多变量可提取为方法参数
        queries.put("param", "**code**:" + code + ",**minute**:5");
        queries.put("smsSignId", properties.getSmsSignId());
        queries.put("templateId", properties.getTemplateId());

        // 3. 执行请求
        try {
            ResponseEntity<String> response = RestClientUtils.doPost(
                    properties.getHost(),
                    properties.getPath(),
                    headers,
                    queries,
                    null,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("SMS sent successfully to: {}", phoneNumber);
                return SmsSendResponse.success();
            } else {
                log.error("SMS Provider returned error status: {}", response.getStatusCode());
                return SmsSendResponse.failure(ResponseCode.SYSTEM_ERROR.name(), "短信网关响应错误");
            }
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", phoneNumber, e);
            // 截断异常信息防止返回过多无用栈给前端
            String errMsg = StringUtils.substring(e.toString(), 0, 200);
            return SmsSendResponse.failure(ResponseCode.SYSTEM_ERROR.name(), "短信发送异常: " + errMsg);
        }
    }
}
