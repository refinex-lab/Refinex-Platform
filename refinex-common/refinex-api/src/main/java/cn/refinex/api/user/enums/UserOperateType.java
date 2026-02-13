package cn.refinex.api.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户操作日志类型枚举
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum UserOperateType {

    /**
     * 冻结账户
     */
    FREEZE("冻结"),

    /**
     * 解冻账户
     */
    UNFREEZE("解冻"),

    /**
     * 用户登录
     */
    LOGIN("登录"),

    /**
     * 用户注册
     */
    REGISTER("注册"),

    /**
     * 激活账户
     */
    ACTIVE("激活"),

    /**
     * 实名认证
     */
    AUTH("实名认证"),

    /**
     * 修改信息
     */
    MODIFY("修改信息");

    /**
     * 操作日志类型描述
     */
    private final String desc;
}
