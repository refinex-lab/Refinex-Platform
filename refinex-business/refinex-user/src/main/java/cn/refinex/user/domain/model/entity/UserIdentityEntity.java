package cn.refinex.user.domain.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户身份实体
 *
 * @author refinex
 */
@Data
public class UserIdentityEntity {

    /**
     * 用户身份ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 身份类型
     */
    private Integer identityType;

    /**
     * 标识符
     */
    private String identifier;

    /**
     * 发行者
     */
    private String issuer;

    /**
     * 凭证
     */
    private String credential;

    /**
     * 凭证算法
     */
    private String credentialAlg;

    /**
     * 是否主要
     */
    private Integer isPrimary;

    /**
     * 是否验证
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
     * 状态
     */
    private Integer status;

    /**
     * 是否删除
     */
    private Integer deleted;
}
