package cn.refinex.base.constant;

import lombok.experimental.UtilityClass;

/**
 * 系统环境配置常量
 *
 * 用于标识 Spring 激活的 Profile，区分开发、测试、预发及生产环境。
 * 使用 {@link UtilityClass} 确保该类不可被实例化及继承。
 *
 * @author refinex
 */
@UtilityClass // 确保该类不可被实例化及继承
public class ProfileConstant {

    /**
     * 开发环境
     */
    public static final String PROFILE_DEV = "dev";

    /**
     * 测试环境
     */
    public static final String PROFILE_TEST = "test";

    /**
     * 预发布环境
     */
    public static final String PROFILE_PRE = "pre";

    /**
     * 生产环境
     */
    public static final String PROFILE_PROD = "prod";
}
