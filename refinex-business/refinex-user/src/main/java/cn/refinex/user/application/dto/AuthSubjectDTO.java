package cn.refinex.user.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录主体 DTO
 *
 * @author refinex
 */
@Data
public class AuthSubjectDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户代码
     */
    private String userCode;

    /**
     * 用户名
     */
    private String username;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 用户状态
     */
    private Integer userStatus;

    /**
     * 主要组织ID
     */
    private Long primaryEstabId;

    /**
     * 登录失败次数
     */
    private Integer loginFailCount;

    /**
     * 锁定直到
     */
    private LocalDateTime lockUntil;

    /**
     * 身份ID
     */
    private Long identityId;

    /**
     * 身份状态
     */
    private Integer identityStatus;

    /**
     * 凭证
     */
    private String credential;

    /**
     * 团队ID
     */
    private Long teamId;

    /**
     * 组织管理员
     */
    private Boolean estabAdmin;
}
