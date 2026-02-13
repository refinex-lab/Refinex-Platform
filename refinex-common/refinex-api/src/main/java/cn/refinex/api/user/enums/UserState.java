package cn.refinex.api.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum UserState {

    /**
     * 初始化 (刚注册，未完善信息)
     */
    INIT("初始化"),

    /**
     * 已认证 (实名认证通过)
     */
    AUTH("已认证"),

    /**
     * 已激活 (账户完全可用)
     */
    ACTIVE("已激活"),

    /**
     * 已冻结 (违规被封禁)
     */
    FROZEN("已冻结");

    /**
     * 状态描述
     */
    private final String desc;
}
