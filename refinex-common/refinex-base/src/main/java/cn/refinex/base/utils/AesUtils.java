package cn.refinex.base.utils;

import lombok.experimental.UtilityClass;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * AES 对称加解密工具类
 * <p>
 * 算法：AES/GCM/NoPadding（AEAD 认证加密，防篡改 + 防 Padding Oracle），
 * 每次加密随机生成 12 字节 Nonce，输出格式为 Base64(Nonce + 密文 + AuthTag)，
 * 确保相同明文产生不同密文。
 * <p>
 * 密钥支持两种格式：
 * <ul>
 *   <li>Hex 字符串（32/48/64 个十六进制字符 → 16/24/32 字节 AES 密钥）</li>
 *   <li>原始 UTF-8 字符串（长度恰好 16/24/32 字节）</li>
 * </ul>
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
    /** 合法的 Hex 密钥字符串长度（对应 AES-128/192/256） */
    private static final Set<Integer> VALID_HEX_LENGTHS = Set.of(32, 48, 64);
    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]+$");

    /**
     * 将密钥字符串解析为 AES 密钥字节数组
     * <p>
     * 自动识别 Hex 格式（32/48/64 字符）并解码，否则按 UTF-8 原始字节处理。
     *
     * @param key 密钥字符串
     * @return AES 密钥字节数组（16/24/32 字节）
     */
    private static byte[] resolveKeyBytes(String key) {
        if (key == null) {
            throw new IllegalArgumentException("AES key must not be null");
        }
        // 如果长度是合法的 Hex 密钥长度且全部是十六进制字符，则按 Hex 解码
        if (VALID_HEX_LENGTHS.contains(key.length()) && HEX_PATTERN.matcher(key).matches()) {
            return HexFormat.of().parseHex(key);
        }
        return key.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * AES-GCM 加密
     *
     * @param plaintext 明文
     * @param key       AES 密钥（Hex 字符串或原始 UTF-8 字符串，最终须为 16/24/32 字节）
     * @return Base64 编码的 Nonce + 密文 + AuthTag；plaintext 为 null 或空时返回 null
     */
    public static String encrypt(String plaintext, String key) {
        if (plaintext == null || plaintext.isEmpty()) {
            return null;
        }
        try {
            byte[] nonce = new byte[GCM_NONCE_LENGTH];
            SECURE_RANDOM.nextBytes(nonce);

            SecretKeySpec keySpec = new SecretKeySpec(resolveKeyBytes(key), ALGORITHM);
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
     * @param key          AES 密钥（Hex 字符串或原始 UTF-8 字符串，须与加密时一致）
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

            SecretKeySpec keySpec = new SecretKeySpec(resolveKeyBytes(key), ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_BIT_LENGTH, nonce);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("AES-GCM decrypt failed", e);
        }
    }

    /**
     * 生成 AES 密钥并返回 Hex 字符串
     *
     * @param keyBitLength 密钥位数：128、192 或 256
     * @return Hex 编码的 AES 密钥字符串
     */
    public static String generateKey(int keyBitLength) {
        if (keyBitLength != 128 && keyBitLength != 192 && keyBitLength != 256) {
            throw new IllegalArgumentException("keyBitLength must be 128, 192 or 256, got: " + keyBitLength);
        }
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(keyBitLength, SECURE_RANDOM);
            byte[] keyBytes = keyGen.generateKey().getEncoded();
            return HexFormat.of().formatHex(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("AES KeyGenerator not available", e);
        }
    }

    /**
     * 命令行入口：生成 AES 密钥
     * <p>
     * 用法：{@code java -cp refinex-base.jar cn.refinex.base.utils.AesUtils [128|192|256]}
     *
     * @param args 可选参数：密钥位数（默认 256）
     */
    public static void main(String[] args) {
        int bits = 256;
        if (args.length > 0) {
            bits = Integer.parseInt(args[0]);
        }
        String key = generateKey(bits);
        System.out.println("AES-" + bits + " Key (Hex): " + key);
        System.out.println("Length: " + key.length() + " hex chars = " + (key.length() / 2) + " bytes");
    }
}
