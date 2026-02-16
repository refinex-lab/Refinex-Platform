package cn.refinex.api.user.model.vo;

import cn.refinex.api.user.enums.UserStatus;
import cn.refinex.api.user.enums.UserType;
import com.github.houbb.sensitive.annotation.strategy.SensitiveStrategyPhone;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户详细信息 VO
 * <p>
 * 包含敏感信息（手机号脱敏）、账户状态、组织归属等。
 * 通常用于 "个人中心" 或 "管理后台"。
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserInfo extends BasicUserInfo {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户编码
     */
    private String userCode;

    /**
     * 用户名（本地账号）
     */
    private String username;

    /**
     * 性别 0未知 1男 2女 3其他
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 主手机号 (自动脱敏处理)
     * 策略: 中间4位替换为 *
     */
    @SensitiveStrategyPhone
    private String primaryPhone;

    /**
     * 手机号是否已验证
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
     * @see UserStatus
     */
    private UserStatus status;

    /**
     * 用户类型
     * @see UserType
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
     * 主组织ID
     */
    private Long primaryEstabId;

    /**
     * 主团队ID
     */
    private Long primaryTeamId;

    /**
     * 是否组织管理员
     */
    private Boolean estabAdmin;
}
