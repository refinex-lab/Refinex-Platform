package cn.refinex.user.domain.model.entity;

import lombok.Data;

/**
 * 登录主体聚合
 *
 * @author refinex
 */
@Data
public class UserAuthSubject {

    /**
     * 用户
     */
    private UserEntity user;

    /**
     * 用户身份
     */
    private UserIdentityEntity identity;

    /**
     * 团队ID
     */
    private Long teamId;

    /**
     * 是否团队管理员
     */
    private Boolean estabAdmin;
}
