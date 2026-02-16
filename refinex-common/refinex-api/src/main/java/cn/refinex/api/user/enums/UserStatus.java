package cn.refinex.api.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举（对应 def_user.status）
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum UserStatus {

    /**
     * 启用
     */
    ENABLED(1, "启用"),

    /**
     * 停用
     */
    DISABLED(2, "停用"),

    /**
     * 锁定
     */
    LOCKED(3, "锁定");

    /**
     * 状态编码
     */
    private final int code;

    /**
     * 状态描述
     */
    private final String desc;

    /**
     * 通过编码解析枚举
     *
     * @param code 编码
     * @return 对应枚举，未匹配返回 null
     */
    public static UserStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
