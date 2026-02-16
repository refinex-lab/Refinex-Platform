package cn.refinex.auth.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录来源
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum LoginSource {

    WEB(1, "Web"),
    APP(2, "App"),
    MINI_APP(3, "小程序"),
    API(4, "API");

    private final int code;
    private final String desc;

    public static LoginSource of(Integer code) {
        if (code == null) {
            return null;
        }
        for (LoginSource value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
