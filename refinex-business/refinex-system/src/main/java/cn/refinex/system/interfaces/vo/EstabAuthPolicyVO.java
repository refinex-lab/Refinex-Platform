package cn.refinex.system.interfaces.vo;

import lombok.Data;

/**
 * 企业认证策略视图对象
 *
 * @author refinex
 */
@Data
public class EstabAuthPolicyVO {

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
     * 锁定分钟数
     */
    private Integer lockMinutes;

    /**
     * 会话超时分钟数
     */
    private Integer sessionTimeoutMinutes;

    /**
     * 备注
     */
    private String remark;
}
