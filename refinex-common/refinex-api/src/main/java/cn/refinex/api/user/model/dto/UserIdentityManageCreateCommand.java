package cn.refinex.api.user.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户身份管理-创建命令
 *
 * @author refinex
 */
@Data
public class UserIdentityManageCreateCommand implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 身份类型 1用户名密码 2手机号短信 3邮箱密码 4邮箱验证码 5微信扫码 6OAuth/OIDC 7SAML 8TOTP
     */
    private Integer identityType;

    /**
     * 身份标识
     */
    private String identifier;

    /**
     * 发行方
     */
    private String issuer;

    /**
     * 凭证（加密后）
     */
    private String credential;

    /**
     * 凭证算法
     */
    private String credentialAlg;

    /**
     * 是否主身份 1是 0否
     */
    private Integer isPrimary;

    /**
     * 是否已验证 1是 0否
     */
    private Integer verified;

    /**
     * 状态 1启用 0停用
     */
    private Integer status;
}
