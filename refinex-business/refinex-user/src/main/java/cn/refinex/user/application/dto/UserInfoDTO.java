package cn.refinex.user.application.dto;

import cn.refinex.api.user.enums.UserStatus;
import cn.refinex.api.user.enums.UserType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户详情 DTO
 *
 * @author refinex
 */
@Data
public class UserInfoDTO {

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
     * 性别
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthday;

    /**
     * 主要手机号
     */
    private String primaryPhone;

    /**
     * 手机号是否验证
     */
    private Boolean phoneVerified;

    /**
     * 主要邮箱
     */
    private String primaryEmail;

    /**
     * 邮箱是否验证
     */
    private Boolean emailVerified;

    /**
     * 用户状态
     */
    private UserStatus status;

    /**
     * 用户类型
     */
    private UserType userType;

    /**
     * 注册时间
     */
    private LocalDateTime registerTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 主要组织ID
     */
    private Long primaryEstabId;

    /**
     * 主要团队ID
     */
    private Long primaryTeamId;

    /**
     * 是否组织管理员
     */
    private Boolean estabAdmin;
}
