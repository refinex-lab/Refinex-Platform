package cn.refinex.auth.domain.entity;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户登录身份
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("def_user_identity")
public class DefUserIdentity extends BaseEntity {

    private Long userId;

    private Integer identityType;

    private String identifier;

    private String issuer;

    private String credential;

    private String credentialAlg;

    private Integer isPrimary;

    private Integer verified;

    private LocalDateTime verifiedAt;

    private LocalDateTime bindTime;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private Integer status;
}
