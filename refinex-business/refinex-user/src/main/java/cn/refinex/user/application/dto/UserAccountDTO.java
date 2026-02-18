package cn.refinex.user.application.dto;

import cn.refinex.api.user.enums.UserStatus;
import cn.refinex.api.user.enums.UserType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户账号信息 DTO
 *
 * @author refinex
 */
@Data
public class UserAccountDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户编码
     */
    private String userCode;

    /**
     * 用户名
     */
    private String username;

    /**
     * 主手机号
     */
    private String primaryPhone;

    /**
     * 手机是否已验证
     */
    private Boolean phoneVerified;

    /**
     * 主邮箱
     */
    private String primaryEmail;

    /**
     * 邮箱是否已验证
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
     * 是否已设置用户名密码
     */
    private Boolean usernamePasswordEnabled;

    /**
     * 是否已设置邮箱密码
     */
    private Boolean emailPasswordEnabled;
}
