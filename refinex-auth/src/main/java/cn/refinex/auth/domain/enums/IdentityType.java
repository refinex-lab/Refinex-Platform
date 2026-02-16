package cn.refinex.auth.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录身份类型（对应 def_user_identity.identity_type）
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum IdentityType {

    USERNAME_PASSWORD(1, "用户名密码"),
    PHONE_SMS(2, "手机号短信"),
    EMAIL_PASSWORD(3, "邮箱密码"),
    EMAIL_CODE(4, "邮箱验证码"),
    WECHAT_QR(5, "微信扫码"),
    OIDC(6, "OAuth/OIDC"),
    SAML(7, "SAML"),
    TOTP(8, "TOTP");

    private final int code;
    private final String desc;

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
