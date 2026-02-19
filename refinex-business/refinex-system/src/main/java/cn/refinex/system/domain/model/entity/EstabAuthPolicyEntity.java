package cn.refinex.system.domain.model.entity;

import lombok.Data;

/**
 * 企业认证策略实体
 *
 * @author refinex
 */
@Data
public class EstabAuthPolicyEntity {

    /**
     * 策略ID
     */
    private Long id;

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 密码登录开关
     */
    private Integer passwordLoginEnabled;

    /**
     * 短信登录开关
     */
    private Integer smsLoginEnabled;

    /**
     * 邮箱登录开关
     */
    private Integer emailLoginEnabled;

    /**
     * 微信登录开关
     */
    private Integer wechatLoginEnabled;

    /**
     * MFA 强制开关
     */
    private Integer mfaRequired;

    /**
     * MFA 方式
     */
    private String mfaMethods;

    /**
     * 密码最小长度
     */
    private Integer passwordMinLen;

    /**
     * 密码强度
     */
    private Integer passwordStrength;

    /**
     * 密码过期天数
     */
    private Integer passwordExpireDays;

    /**
     * 登录失败阈值
     */
    private Integer loginFailThreshold;

    /**
     * 锁定时长（分钟）
     */
    private Integer lockMinutes;

    /**
     * 会话超时（分钟）
     */
    private Integer sessionTimeoutMinutes;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否删除
     */
    private Integer deleted;
}
