package cn.refinex.api.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户权限等级枚举
 * <p>
 * 用于网关鉴权时粗粒度判断用户权限范围。
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum UserPermission {

    /**
     * 基础权限 (登录即可拥有的权限)
     */
    BASIC("基础权限"),

    /**
     * 认证权限 (完成实名认证后拥有的权限，如交易)
     */
    AUTH("实名权限"),

    /**
     * 冻结权限 (被冻结后仅剩的只读权限)
     */
    FROZEN("冻结权限"),

    /**
     * 无权限 (游客)
     */
    NONE("无权限");

    /**
     * 权限描述
     */
    private final String desc;
}
