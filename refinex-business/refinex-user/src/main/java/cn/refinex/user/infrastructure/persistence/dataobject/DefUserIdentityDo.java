package cn.refinex.user.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户身份 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("def_user_identity")
public class DefUserIdentityDo extends BaseEntity {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 身份类型
     */
    private Integer identityType;

    /**
     * 标识
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
     * 最后登录 IP
     */
    private String lastLoginIp;

    /**
     * 状态
     */
    private Integer status;
}
