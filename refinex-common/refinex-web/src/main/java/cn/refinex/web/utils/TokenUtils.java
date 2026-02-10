package cn.refinex.web.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

import static cn.refinex.cache.constant.CacheConstant.CACHE_KEY_SEPARATOR;

/**
 * Token 加解密工具类
 * <p>
 * 用于生成带有防重放机制（UUID后缀）的加密 Token。
 * <p>
 * 核心逻辑：Token = AES( 原始Key + ":" + UUID )
 *
 * @author refinex
 */
@Slf4j
@UtilityClass
public class TokenUtils {

    /**
     * AES 加密密钥 (128位/16字节)
     * <p>
     * 建议通过 @Value("${refinex.token.secret}") 注入，或者从环境变量读取。
     */
    private static final String TOKEN_AES_KEY_STR = "tokenbyrefinex";

    /**
     * 预处理 Key 的字节数组，避免每次调用重复转换
     */
    private static final byte[] AES_KEY_BYTES = TOKEN_AES_KEY_STR.getBytes(StandardCharsets.UTF_8);

    /**
     * 根据业务 Key 生成加密 Token
     *
     * @param bizKey 业务键 (如: "token:buy:29:10085")
     * @return 加密后的 Base64 字符串
     */
    public static String generateToken(String bizKey) {
        if (!StringUtils.hasText(bizKey)) {
            return null;
        }

        // 生成 UUID (Hutool simpleUUID 更快且无横杠，节省空间)
        String uuid = IdUtil.simpleUUID();

        // 拼接格式：业务Key:UUID
        // 目的：即使同一个业务Key，每次生成的 Token 也是随机且唯一的
        String rawToken = bizKey + CACHE_KEY_SEPARATOR + uuid;

        // 加密
        return getAes().encryptBase64(rawToken);
    }

    /**
     * 解析 Token 获取业务 Key
     *
     * @param encryptedToken 加密后的 Token 字符串
     * @return 原始业务 Key (即去除了 UUID 后缀的部分)，如果解密失败返回 null
     */
    public static String parseToken(String encryptedToken) {
        if (!StringUtils.hasText(encryptedToken)) {
            return null;
        }

        try {
            // 1. 解密
            String rawToken = getAes().decryptStr(encryptedToken);

            if (!StringUtils.hasText(rawToken)) {
                return null;
            }

            // 2. 移除 UUID 后缀
            // 逻辑：找到最后一个 ":" 的位置，截取前面的部分
            int lastSeparatorIndex = rawToken.lastIndexOf(CACHE_KEY_SEPARATOR);

            if (lastSeparatorIndex == -1) {
                log.warn("Invalid token format (separator not found): {}", rawToken);
                return null;
            }

            return rawToken.substring(0, lastSeparatorIndex);
        } catch (Exception e) {
            // 捕获解密异常（如 Token 被篡改、格式错误等），只打印 Warn 日志，不中断业务
            log.warn("Failed to decrypt token: {}", encryptedToken, e);
            return null;
        }
    }

    /**
     * 获取 AES 实例
     * <p>
     * Hutool 的 AES 对象不是完全线程安全的（取决于模式），
     * 为了高并发下的绝对安全，建议每次使用时创建新实例，或者使用 ThreadLocal。
     * 由于 new AES 开销极小（Key 已预处理），此处选择直接创建。
     */
    private static AES getAes() {
        return SecureUtil.aes(AES_KEY_BYTES);
    }
}
