package cn.refinex.api.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账户主体类型枚举
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum UserType {

    /**
     * C端真实用户
     */
    CUSTOMER("个人用户"),

    /**
     * 平台系统账户
     */
    PLATFORM("平台账户");

    /**
     * 账户主体类型描述
     */
    private final String desc;
}
