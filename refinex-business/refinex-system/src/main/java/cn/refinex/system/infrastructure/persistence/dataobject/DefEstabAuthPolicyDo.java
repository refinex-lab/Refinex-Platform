package cn.refinex.system.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业认证策略 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("def_estab_auth_policy")
public class DefEstabAuthPolicyDo extends BaseEntity {

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
     * 强制MFA
     */
    private Integer mfaRequired;

    /**
     * MFA方式
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
     * 连续失败阈值
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
