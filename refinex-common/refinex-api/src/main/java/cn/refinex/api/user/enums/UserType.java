package cn.refinex.api.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举（对应 def_user.user_type）
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum UserType {

    /**
     * 平台用户
     */
    PLATFORM(0, "平台"),

    /**
     * 租户用户
     */
    TENANT(1, "租户"),

    /**
     * 合作方用户
     */
    PARTNER(2, "合作方");

    /**
     * 类型编码
     */
    private final int code;

    /**
     * 类型描述
     */
    private final String desc;

    /**
     * 通过编码解析枚举
     *
     * @param code 编码
     * @return 对应枚举，未匹配返回 null
     */
    public static UserType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
