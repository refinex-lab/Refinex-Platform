package cn.refinex.user.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 身份类型
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum IdentityType {

    /**
     * 用户名密码
     */
    USERNAME_PASSWORD(1),

    /**
     * 手机短信
     */
    PHONE_SMS(2),

    /**
     * 邮箱密码
     */
    EMAIL_PASSWORD(3),

    /**
     * 邮箱验证码
     */
    EMAIL_CODE(4),

    /**
     * 微信二维码
     */
    WECHAT_QR(5),

    /**
     * OIDC
     */
    OIDC(6),

    /**
     * SAML
     */
    SAML(7),

    /**
     * TOTP
     */
    TOTP(8);

    /**
     * 代码
     */
    private final int code;

    /**
     * 根据代码获取身份类型
     *
     * @param code 代码
     * @return 身份类型
     */
    public static IdentityType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (IdentityType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
