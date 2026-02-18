package cn.refinex.user.domain.model.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @author refinex
 */
@Data
public class UserEntity {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户编号
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
     * 头像链接
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
     * 用户类型
     */
    private Integer userType;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 主要机构ID
     */
    private Long primaryEstabId;

    /**
     * 主要手机号
     */
    private String primaryPhone;

    /**
     * 手机号是否验证
     */
    private Integer phoneVerified;

    /**
     * 主要邮箱
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
     * 登录失败次数
     */
    private Integer loginFailCount;

    /**
     * 锁定截止时间
     */
    private LocalDateTime lockUntil;

    /**
     * 是否删除
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;
}
