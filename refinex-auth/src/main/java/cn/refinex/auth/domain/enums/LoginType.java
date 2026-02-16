package cn.refinex.auth.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录方式
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum LoginType {

    USERNAME_PASSWORD(1, "用户名密码", IdentityType.USERNAME_PASSWORD),
    PHONE_SMS(2, "手机号短信", IdentityType.PHONE_SMS),
    EMAIL_PASSWORD(3, "邮箱密码", IdentityType.EMAIL_PASSWORD),
    EMAIL_CODE(4, "邮箱验证码", IdentityType.EMAIL_CODE),
    WECHAT_QR(5, "微信扫码", IdentityType.WECHAT_QR),
    OIDC(6, "OAuth/OIDC", IdentityType.OIDC),
    SAML(7, "SAML", IdentityType.SAML),
    TOTP(8, "TOTP", IdentityType.TOTP);

    private final int code;
    private final String desc;
    private final IdentityType identityType;

    public static LoginType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (LoginType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
