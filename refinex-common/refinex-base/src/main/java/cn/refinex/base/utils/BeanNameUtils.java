package cn.refinex.base.utils;

import com.google.common.base.CaseFormat;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/**
 * Bean 名称生成工具类
 * <p>
 * 用于根据策略名称动态生成 Spring Bean 的名称，常用于策略模式的工厂实现。
 *
 * @author refinex
 */
@UtilityClass
public class BeanNameUtils {

    /**
     * 将策略标识转换为标准的 Spring Bean 名称（小写驼峰）
     * <p>
     * 示例：
     * <pre>{@code
     * getBeanName("WEN_CHANG", "ChainService") -> "wenChangChainService"
     * getBeanName("ALI_PAY", "PaymentHandler") -> "aliPayPaymentHandler"
     * }</pre>
     *
     * @param strategyName  策略名称（通常为大写下划线格式，如 WEN_CHANG）
     * @param serviceSuffix 服务后缀（如 ChainService）
     * @return 拼接后的 Bean 名称
     * @throws IllegalArgumentException 如果参数为空
     */
    public static String getBeanName(String strategyName, String serviceSuffix) {
        if (StringUtils.isAnyBlank(strategyName, serviceSuffix)) {
            throw new IllegalArgumentException("Strategy name and service suffix must not be blank");
        }

        // 使用 Guava 将 UPPER_UNDERSCORE (WEN_CHANG) 转为 LOWER_CAMEL (wenChang)
        String prefix = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, strategyName);
        return prefix + serviceSuffix;
    }
}
