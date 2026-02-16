package cn.refinex.auth.domain.entity;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 组织认证与安全策略
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("def_estab_auth_policy")
public class DefEstabAuthPolicy extends BaseEntity {

    private Long estabId;

    private Integer passwordLoginEnabled;

    private Integer smsLoginEnabled;

    private Integer emailLoginEnabled;

    private Integer wechatLoginEnabled;

    private Integer mfaRequired;

    private String mfaMethods;

    private Integer passwordMinLen;

    private Integer passwordStrength;

    private Integer passwordExpireDays;

    private Integer loginFailThreshold;

    private Integer lockMinutes;

    private Integer sessionTimeoutMinutes;

    private String remark;
}
