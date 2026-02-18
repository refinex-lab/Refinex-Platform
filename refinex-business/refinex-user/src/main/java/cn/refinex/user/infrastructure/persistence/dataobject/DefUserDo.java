package cn.refinex.user.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("def_user")
public class DefUserDo extends BaseEntity {

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
     * 头像 URL
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
     * 主要企业 ID
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
     * 最后登录 IP
     */
    private String lastLoginIp;

    /**
     * 登录失败次数
     */
    private Integer loginFailCount;

    /**
     * 锁定直到
     */
    private LocalDateTime lockUntil;
}
