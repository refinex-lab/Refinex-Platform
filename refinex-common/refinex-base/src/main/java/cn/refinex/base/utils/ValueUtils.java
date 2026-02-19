package cn.refinex.base.utils;

import lombok.experimental.UtilityClass;

/**
 * 通用值处理工具
 *
 * @author refinex
 */
@UtilityClass
public class ValueUtils {

    /**
     * 判断字符串是否为空白
     *
     * @param value 字符串
     * @return true-空白，false-非空白
     */
    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 字符串去空格后转 null
     *
     * @param value 字符串
     * @return 去空格后的值，空白返回 null
     */
    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 默认值处理
     *
     * @param value        原值
     * @param defaultValue 默认值
     * @param <T>          类型
     * @return value 为 null 时返回 defaultValue，否则返回 value
     */
    public static <T> T defaultIfNull(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}
