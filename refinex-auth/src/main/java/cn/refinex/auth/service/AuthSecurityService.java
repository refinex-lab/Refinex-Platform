package cn.refinex.auth.service;

import cn.refinex.auth.config.AuthProperties;
import cn.refinex.auth.domain.error.AuthErrorCode;
import cn.refinex.auth.domain.model.LoginContext;
import cn.refinex.base.exception.BizException;
import cn.refinex.limiter.api.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 认证安全防护
 *
 * @author refinex
 */
@Service
@RequiredArgsConstructor
public class AuthSecurityService {

    private final RateLimiter rateLimiter;
    private final AuthProperties authProperties;

    /**
     * 检查发送短信验证码的频率
     *
     * @param phone 手机号
     * @param context 登录上下文
     */
    public void checkSmsSend(String phone, LoginContext context) {
        String ip = normalize(context == null ? null : context.getIp());
        if (!rateLimiter.tryAcquire("auth:sms:ip:" + ip, authProperties.getSmsIpLimit(), authProperties.getSmsWindowSeconds())) {
            throw new BizException(AuthErrorCode.SMS_SEND_TOO_FREQUENT);
        }
        if (phone != null && !rateLimiter.tryAcquire("auth:sms:phone:" + phone, authProperties.getSmsPhoneLimit(), authProperties.getSmsWindowSeconds())) {
            throw new BizException(AuthErrorCode.SMS_SEND_TOO_FREQUENT);
        }
        String deviceId = normalize(context == null ? null : context.getDeviceId());
        if (!rateLimiter.tryAcquire("auth:sms:device:" + deviceId, authProperties.getSmsDeviceLimit(), authProperties.getSmsWindowSeconds())) {
            throw new BizException(AuthErrorCode.SMS_SEND_TOO_FREQUENT);
        }
    }

    /**
     * 检查注册的频率
     *
     * @param identifier 注册标识符
     * @param context 登录上下文
     */
    public void checkRegister(String identifier, LoginContext context) {
        String ip = normalize(context == null ? null : context.getIp());
        if (!rateLimiter.tryAcquire("auth:reg:ip:" + ip, authProperties.getRegisterIpLimit(), authProperties.getRegisterWindowSeconds())) {
            throw new BizException(AuthErrorCode.REGISTER_TOO_FREQUENT);
        }
        if (identifier != null && !rateLimiter.tryAcquire("auth:reg:id:" + identifier, authProperties.getRegisterIdentifierLimit(), authProperties.getRegisterWindowSeconds())) {
            throw new BizException(AuthErrorCode.REGISTER_TOO_FREQUENT);
        }
    }

    /**
     * 检查登录的频率
     *
     * @param identifier 登录标识符
     * @param context 登录上下文
     */
    public void checkLogin(String identifier, LoginContext context) {
        String ip = normalize(context == null ? null : context.getIp());
        if (!rateLimiter.tryAcquire("auth:login:ip:" + ip, authProperties.getLoginIpLimit(), authProperties.getLoginWindowSeconds())) {
            throw new BizException(AuthErrorCode.LOGIN_TOO_FREQUENT);
        }
        if (identifier != null && !rateLimiter.tryAcquire("auth:login:id:" + identifier, authProperties.getLoginIdentifierLimit(), authProperties.getLoginWindowSeconds())) {
            throw new BizException(AuthErrorCode.LOGIN_TOO_FREQUENT);
        }
    }

    /**
     * 规范化值
     *
     * @param value 值
     * @return 规范化后的值
     */
    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.trim();
    }
}
