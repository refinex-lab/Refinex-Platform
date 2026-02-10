package cn.refinex.base.utils;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 货币转换工具类
 * <p>
 * 提供元与分之间的转换。
 * <p>
 * <strong>注意：</strong> 本工具类默认按 1 元 = 100 分的汇率计算（适用于 CNY, USD 等）。
 * 不适用于日元（JPY）、韩元（KRW）等无小数位币种。如需支持多币种，请使用专门的 Money 领域对象。
 *
 * @author refinex
 */
@UtilityClass
public class MoneyUtils {

    /**
     * 转换倍率：100
     */
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * 元转分
     * <p>
     * 将 BigDecimal 的元转换为 Long 类型的分。
     * 采用四舍五入模式。
     *
     * @param amountYuan 金额（元）
     * @return 金额（分），如果输入为 null 则返回 null
     */
    public static Long yuanToCent(BigDecimal amountYuan) {
        if (amountYuan == null) {
            return null;
        }

        // 建议先 setScale 再转 long，避免由 1.001 变成 100 的非预期截断，这里采用四舍五入
        return amountYuan.multiply(HUNDRED)
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

    /**
     * 分转元
     * <p>
     * 将 Long 类型的分转换为 BigDecimal 类型的元。
     * 保留两位小数，四舍五入。
     *
     * @param amountCent 金额（分）
     * @return 金额（元），如果输入为 null 则返回 null
     */
    public static BigDecimal centToYuan(Long amountCent) {
        if (amountCent == null) {
            return null;
        }

        return new BigDecimal(amountCent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }
}
