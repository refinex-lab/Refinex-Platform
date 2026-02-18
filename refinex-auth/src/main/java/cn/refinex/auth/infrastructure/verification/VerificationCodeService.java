package cn.refinex.auth.infrastructure.verification;

import cn.refinex.auth.config.AuthProperties;
import cn.refinex.auth.domain.error.AuthErrorCode;
import cn.refinex.base.exception.BizException;
import cn.refinex.mail.api.MailTemplateService;
import cn.refinex.mail.response.MailSendResponse;
import cn.refinex.sms.api.SmsService;
import cn.refinex.sms.response.SmsSendResponse;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Year;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 *
 * @author refinex
 */
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthProperties authProperties;
    private final SmsService smsService;
    private final MailTemplateService mailTemplateService;
    private final CacheManager cacheManager;

    /**
     * 短信验证码缓存
     */
    private Cache<String, String> smsCodeCache;

    /**
     * 短信冷却缓存
     */
    private Cache<String, String> smsCooldownCache;

    /**
     * 邮箱验证码缓存
     */
    private Cache<String, String> emailCodeCache;

    /**
     * 邮箱冷却缓存
     */
    private Cache<String, String> emailCooldownCache;

    /**
     * 初始化缓存
     */
    @PostConstruct
    public void initCaches() {
        // 创建短信验证码缓存
        this.smsCodeCache = cacheManager.getOrCreateCache(
                QuickConfig.newBuilder("auth:sms:code:")
                        .cacheType(CacheType.BOTH)
                        .expire(Duration.ofSeconds(authProperties.getSmsCodeExpireSeconds()))
                        .build()
        );
        // 创建短信冷却缓存
        this.smsCooldownCache = cacheManager.getOrCreateCache(
                QuickConfig.newBuilder("auth:sms:cooldown:")
                        .cacheType(CacheType.BOTH)
                        .expire(Duration.ofSeconds(authProperties.getSmsCodeIntervalSeconds()))
                        .build()
        );

        // 创建邮箱验证码缓存
        this.emailCodeCache = cacheManager.getOrCreateCache(
                QuickConfig.newBuilder("auth:email:code:")
                        .cacheType(CacheType.BOTH)
                        .expire(Duration.ofSeconds(authProperties.getEmailCodeExpireSeconds()))
                        .build()
        );
        // 创建邮箱冷却缓存
        this.emailCooldownCache = cacheManager.getOrCreateCache(
                QuickConfig.newBuilder("auth:email:cooldown:")
                        .cacheType(CacheType.BOTH)
                        .expire(Duration.ofSeconds(authProperties.getEmailCodeIntervalSeconds()))
                        .build()
        );
    }

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @param scene 场景
     */
    public void sendSmsCode(String phone, String scene) {
        String cooldownKey = scene + ":" + phone;
        if (smsCooldownCache.get(cooldownKey) != null) {
            throw new BizException(AuthErrorCode.CODE_SEND_TOO_FREQUENT);
        }

        String code = generateCode(authProperties.getSmsCodeLength());
        SmsSendResponse response = smsService.sendMsg(phone, code);
        if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
            throw new BizException(AuthErrorCode.SYSTEM_ERROR);
        }

        String codeKey = scene + ":" + phone;
        smsCodeCache.put(codeKey, code, authProperties.getSmsCodeExpireSeconds(), TimeUnit.SECONDS);
        smsCooldownCache.put(cooldownKey, "1", authProperties.getSmsCodeIntervalSeconds(), TimeUnit.SECONDS);
    }

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱
     * @param scene 场景
     */
    public void sendEmailCode(String email, String scene) {
        String normalizedEmail = normalizeEmail(email);
        String cooldownKey = scene + ":" + normalizedEmail;
        if (emailCooldownCache.get(cooldownKey) != null) {
            throw new BizException(AuthErrorCode.CODE_SEND_TOO_FREQUENT);
        }

        String code = generateCode(authProperties.getEmailCodeLength());
        Map<String, Object> variables = new HashMap<>(8);
        variables.put("brandName", authProperties.getMailBrandName());
        variables.put("sceneName", resolveSceneName(scene));
        variables.put("code", code);
        variables.put("expireMinutes", Math.max(1, authProperties.getEmailCodeExpireSeconds() / 60));
        variables.put("year", Year.now().getValue());

        MailSendResponse response = mailTemplateService.sendTemplate(
                normalizedEmail,
                authProperties.getEmailCodeSubject(),
                authProperties.getEmailCodeTemplateName(),
                variables
        );
        if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
            throw new BizException(AuthErrorCode.SYSTEM_ERROR);
        }

        String codeKey = scene + ":" + normalizedEmail;
        emailCodeCache.put(codeKey, code, authProperties.getEmailCodeExpireSeconds(), TimeUnit.SECONDS);
        emailCooldownCache.put(cooldownKey, "1", authProperties.getEmailCodeIntervalSeconds(), TimeUnit.SECONDS);
    }

    /**
     * 验证短信验证码
     *
     * @param phone 手机号
     * @param scene 场景
     * @param code 验证码
     * @return 是否验证成功
     */
    public boolean verifySmsCode(String phone, String scene, String code) {
        if (code == null || code.isBlank()) {
            return false;
        }

        String codeKey = scene + ":" + phone;
        String cached = smsCodeCache.get(codeKey);
        if (cached != null && cached.equalsIgnoreCase(code)) {
            smsCodeCache.remove(codeKey);
            return true;
        }
        return false;
    }

    /**
     * 验证邮箱验证码
     *
     * @param email 邮箱
     * @param scene 场景
     * @param code 验证码
     * @return 是否验证成功
     */
    public boolean verifyEmailCode(String email, String scene, String code) {
        if (code == null || code.isBlank()) {
            return false;
        }

        String codeKey = scene + ":" + normalizeEmail(email);
        String cached = emailCodeCache.get(codeKey);
        if (cached != null && cached.equalsIgnoreCase(code)) {
            emailCodeCache.remove(codeKey);
            return true;
        }
        return false;
    }

    /**
     * 生成指定长度的随机验证码
     *
     * @param length 验证码长度
     * @return 随机验证码
     */
    private String generateCode(int length) {
        int len = Math.max(4, length);
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 场景名
     *
     * @param scene 场景编码
     * @return 场景名称
     */
    private String resolveSceneName(String scene) {
        if ("register".equalsIgnoreCase(scene)) {
            return "注册";
        }
        if ("reset".equalsIgnoreCase(scene)) {
            return "重置密码";
        }
        return "登录";
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }
}
