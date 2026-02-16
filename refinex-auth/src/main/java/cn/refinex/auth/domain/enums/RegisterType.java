package cn.refinex.auth.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 注册类型
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum RegisterType {

    USERNAME(1, "用户名"),
    PHONE(2, "手机号"),
    EMAIL(3, "邮箱");

    private final int code;
    private final String desc;

    public static RegisterType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (RegisterType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
