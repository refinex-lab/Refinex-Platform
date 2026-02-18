package cn.refinex.user.domain.model.enums;

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

    /**
     * 用户名
     */
    USERNAME(1),

    /**
     * 手机
     */
    PHONE(2),

    /**
     * 邮箱
     */
    EMAIL(3);

    /**
     * 代码
     */
    private final int code;

    /**
     * 根据代码获取注册类型
     *
     * @param code 代码
     * @return 注册类型
     */
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
