package cn.refinex.api.user.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户管理 DTO
 *
 * @author refinex
 */
@Data
public class UserManageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 显示名称
     */
    private String displayName;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 用户状态
     */
    private Integer status;

    /**
     * 主组织ID
     */
    private Long primaryEstabId;

    /**
     * 主组织名称
     */
    private String primaryEstabName;

    /**
     * 主手机号
     */
    private String primaryPhone;

    /**
     * 手机号是否验证
     */
    private Integer phoneVerified;

    /**
     * 主邮箱
     */
    private String primaryEmail;

    /**
     * 邮箱是否验证
     */
    private Integer emailVerified;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 连续失败次数
     */
    private Integer loginFailCount;

    /**
     * 锁定截止时间
     */
    private LocalDateTime lockUntil;

    /**
     * 备注
     */
    private String remark;
}
