package cn.refinex.web.context;

import lombok.experimental.UtilityClass;

/**
 * 用户上下文容器
 * <p>
 * 基于 ThreadLocal 存储当前请求的用户信息、压测标记等。
 * 必须确保在 Filter/Interceptor 的 finally 块中调用 clear()，防止内存泄漏或线程复用导致的数据污染。
 *
 * @author refinex
 */
@UtilityClass
public class UserContext {

    /**
     * 存储当前 Token (或解析后的 UserID/OpenID)
     */
    private static final ThreadLocal<String> TOKEN_HOLDER = new ThreadLocal<>();

    /**
     * 存储压测标记
     */
    private static final ThreadLocal<Boolean> STRESS_TEST_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前请求的 Token
     *
     * @param token Token
     */
    public static void setToken(String token) {
        TOKEN_HOLDER.set(token);
    }

    /**
     * 获取当前请求的 Token
     *
     * @return Token
     */
    public static String getToken() {
        return TOKEN_HOLDER.get();
    }

    /**
     * 设置压测标记
     *
     * @param isStress 是否压测
     */
    public static void setStressTest(boolean isStress) {
        STRESS_TEST_HOLDER.set(isStress);
    }

    /**
     * 是否压测
     *
     * @return 是否压测
     */
    public static boolean isStressTest() {
        return Boolean.TRUE.equals(STRESS_TEST_HOLDER.get());
    }

    /**
     * 清理上下文
     */
    public static void clear() {
        TOKEN_HOLDER.remove();
        STRESS_TEST_HOLDER.remove();
    }
}
