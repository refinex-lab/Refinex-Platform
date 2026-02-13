package cn.refinex.api.user.model.vo;

import cn.refinex.api.user.enums.UserRole;
import cn.refinex.api.user.enums.UserState;
import com.github.houbb.sensitive.annotation.strategy.SensitiveStrategyPhone;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 用户详细信息 VO
 * <p>
 * 包含敏感信息（手机号脱敏）、状态信息等。
 * 通常用于 "个人中心" 或 "网关鉴权上下文"。
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserInfo extends BasicUserInfo {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 手机号 (自动脱敏处理)
     * 策略: 中间4位替换为 *
     */
    @SensitiveStrategyPhone
    private String telephone;

    /**
     * 用户状态
     * @see UserState
     */
    private String state;

    /**
     * 是否已通过实名认证
     */
    private Boolean certification;

    /**
     * 用户角色
     */
    private UserRole userRole;

    /**
     * 注册时间
     */
    private LocalDateTime createTime;
}
