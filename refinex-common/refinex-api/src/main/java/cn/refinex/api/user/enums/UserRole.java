package cn.refinex.api.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum UserRole {

    /**
     * 普通用户 (C端用户)
     */
    CUSTOMER("普通用户"),

    /**
     * 创作者/艺术家
     */
    ARTIST("艺术家"),

    /**
     * 平台管理员
     */
    ADMIN("管理员");

    /**
     * 角色描述
     */
    private final String desc;
}
