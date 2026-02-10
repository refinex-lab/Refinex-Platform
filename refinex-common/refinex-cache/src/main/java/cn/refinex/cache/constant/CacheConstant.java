package cn.refinex.cache.constant;

import lombok.experimental.UtilityClass;

/**
 * 缓存相关常量定义
 *
 * @author refinex
 */
@UtilityClass
public class CacheConstant {

    /**
     * Redis Key 分隔符
     * <p>
     * 用于区分业务模块和层级，例如: "USER:INFO:1001"
     */
    public static final String CACHE_KEY_SEPARATOR = ":";
}
