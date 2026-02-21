package cn.refinex.base.utils;

import lombok.experimental.UtilityClass;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES 对称加解密工具类
 * <p>
 * 算法：AES/GCM/NoPadding（AEAD 认证加密，防篡改 + 防 Padding Oracle），
 * 每次加密随机生成 12 字节 Nonce，输出格式为 Base64(Nonce + 密文 + AuthTag)，
 * 确保相同明文产生不同密文。
 * <p>
 * 密钥长度支持 16/24/32 字节（AES-128/192/256），由调用方配置决定。
 *
 * @author refinex
 */
@UtilityClass
public final class AesUtils {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    /** GCM 推荐 Nonce 长度 12 字节 */
    private static final int GCM_NONCE_LENGTH = 12;
    /** GCM 认证标签长度 128 位 */
    private static final int GCM_TAG_BIT_LENGTH = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * AES-GCM 加密
     *
     * @param plaintext 明文
     * @param key       AES 密钥（16/24/32 字节）
     * @return Base64 编码的 Nonce + 密文 + AuthTag；plaintext 为 null 或空时返回 null
     */
    public static String encrypt(String plaintext, String key) {
        if (plaintext == null || plaintext.isEmpty()) {
            return null;
        }
        try {
            byte[] nonce = new byte[GCM_NONCE_LENGTH];
            SECURE_RANDOM.nextBytes(nonce);

            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_BIT_LENGTH, nonce);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Nonce + 密文（含 AuthTag）拼接后 Base64 编码
            byte[] result = new byte[GCM_NONCE_LENGTH + encrypted.length];
            System.arraycopy(nonce, 0, result, 0, GCM_NONCE_LENGTH);
            System.arraycopy(encrypted, 0, result, GCM_NONCE_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new IllegalStateException("AES-GCM encrypt failed", e);
        }
    }

    /**
     * AES-GCM 解密
     *
     * @param cipherBase64 Base64 编码的 Nonce + 密文 + AuthTag
     * @param key          AES 密钥（16/24/32 字节，须与加密时一致）
     * @return 解密后的明文；cipherBase64 为 null 或空时返回 null
     */
    public static String decrypt(String cipherBase64, String key) {
        if (cipherBase64 == null || cipherBase64.isEmpty()) {
            return null;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherBase64);

            byte[] nonce = new byte[GCM_NONCE_LENGTH];
            System.arraycopy(decoded, 0, nonce, 0, GCM_NONCE_LENGTH);

            byte[] encrypted = new byte[decoded.length - GCM_NONCE_LENGTH];
            System.arraycopy(decoded, GCM_NONCE_LENGTH, encrypted, 0, encrypted.length);

            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_BIT_LENGTH, nonce);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("AES-GCM decrypt failed", e);
        }
    }
}
