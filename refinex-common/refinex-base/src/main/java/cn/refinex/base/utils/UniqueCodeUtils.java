package cn.refinex.base.utils;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;

/**
 * 统一唯一编码生成工具
 * <p>
 * 说明：
 * 1. 仅负责生成格式稳定的候选编码（大写字母 + 数字）。
 * 2. 业务唯一性需要由调用方在持久化前做去重校验（例如数据库唯一索引/重复检查）。
 *
 * @author refinex
 */
@UtilityClass
public final class UniqueCodeUtils {

    private static final char[] UPPER_ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成大写唯一编码（无前缀）
     *
     * @param randomLength 随机段长度（必须大于0）
     * @return 编码
     */
    public static String randomUpperCode(int randomLength) {
        return randomUpperCode(null, randomLength);
    }

    /**
     * 生成大写唯一编码（带前缀）
     *
     * @param prefix       前缀（可为空）
     * @param randomLength 随机段长度（必须大于0）
     * @return 编码
     */
    public static String randomUpperCode(String prefix, int randomLength) {
        if (randomLength <= 0) {
            throw new IllegalArgumentException("randomLength must be greater than 0");
        }

        String safePrefix = prefix == null ? "" : prefix.trim().toUpperCase();
        StringBuilder builder = new StringBuilder(safePrefix.length() + randomLength);
        builder.append(safePrefix);

        for (int i = 0; i < randomLength; i++) {
            builder.append(UPPER_ALPHANUMERIC[RANDOM.nextInt(UPPER_ALPHANUMERIC.length)]);
        }

        return builder.toString();
    }
}
