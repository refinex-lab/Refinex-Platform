package cn.refinex.system.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户身份视图对象
 *
 * @author refinex
 */
@Data
public class SystemUserIdentityVO {

    /**
     * 身份ID
     */
    private Long identityId;

    /**
     * 用户ID
     */
    private Long userId;

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
     * 验证时间
     */
    private LocalDateTime verifiedAt;

    /**
     * 绑定时间
     */
    private LocalDateTime bindTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 状态 1启用 0停用
     */
    private Integer status;
}
