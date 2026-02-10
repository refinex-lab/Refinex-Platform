package cn.refinex.web.filter;

import cn.refinex.web.context.UserContext;
import cn.refinex.web.utils.TokenUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 * Token 校验过滤器
 * <p>
 * 核心职责：
 * 1. 拦截 HTTP 请求，提取 Authorization 头。
 * 2. 验证 Token 的合法性（防重放、防篡改）。
 * 3. 将验证通过的 Token 信息存入 UserContext。
 *
 * @author refinex
 */
@Slf4j
@Component // 纳入 Spring 管理
@Order(1)  // 保证优先级，通常越小越靠前
public class TokenFilter implements Filter {

    /**
     * HTTP 头：Authorization
     */
    private static final String HEADER_AUTH = "Authorization";

    /**
     * HTTP 头：isStress
     */
    private static final String HEADER_STRESS = "isStress";

    /**
     * 非法值：null
     */
    private static final String INVALID_VALUE_NULL = "null";

    /**
     * 非法值：undefined
     */
    private static final String INVALID_VALUE_UNDEFINED = "undefined";

    /**
     * Redisson 客户端
     */
    private final RedissonClient redissonClient;

    /**
     * 压测模式开关
     * 生产环境必须设置为 false，防止安全后门
     */
    @Value("${refinex.security.stress-mode-enabled:false}")
    private boolean stressModeEnabled;

    /**
     * 构造函数注入
     *
     * @param redissonClient Redisson 客户端
     */
    public TokenFilter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 过滤器核心方法
     *
     * @param servletRequest  Servlet 请求
     * @param servletResponse Servlet 响应
     * @param filterChain     过滤器链
     * @throws IOException      IO 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        try {
            // 1. 获取请求头
            String token = httpRequest.getHeader(HEADER_AUTH);
            // 仅在配置开启时，才允许读取压测 Header
            boolean isStress = stressModeEnabled && BooleanUtils.toBoolean(httpRequest.getHeader(HEADER_STRESS));

            // 2. 基础非空校验
            if (isInvalidToken(token) && !isStress) {
                sendUnauthorized(httpResponse, "Token is missing or invalid format");
                return;
            }

            // 3. 执行核心校验逻辑
            String validTokenResult = validateAndConsumeToken(token, isStress);

            if (validTokenResult == null) {
                sendUnauthorized(httpResponse, "Token is expired or invalid");
                return;
            }

            // 4. 设置上下文
            UserContext.setToken(validTokenResult);
            UserContext.setStressTest(isStress);

            // 5. 放行
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            log.error("TokenFilter internal error", e);
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
        } finally {
            // 6. 清理 ThreadLocal，防止内存泄漏
            UserContext.clear();
        }
    }

    /**
     * 校验并消费 Token (核心业务逻辑)
     *
     * @param token    前端传来的 Token
     * @param isStress 是否压测模式
     * @return 校验通过返回 Token 值(或业务ID)，失败返回 null
     */
    private String validateAndConsumeToken(String token, boolean isStress) {
        // A. 压测模式：直接放行，返回模拟数据
        if (isStress) {
            return UUID.randomUUID().toString();
        }

        // B. 正常模式
        // 1. 解析 Token 获取 Redis Key (即去除 UUID 后缀的部分)
        // Token 格式为：AES( 业务Key:UUID )
        String redisKey = TokenUtils.parseToken(token);

        if (redisKey == null) {
            log.warn("Token parse failed: {}", token);
            return null;
        }

        // 2. 执行 Lua 脚本：比对并删除 (One-Time Token / Nonce 机制)
        // 逻辑：获取 Key 对应的 Value，如果 Value 等于传入的 Token (或者原始值)，则删除 Key 并返回 Value
        // 注意：这里需要确保 TokenUtils 生成逻辑和 Redis 存储逻辑是否一致。
        // 假设 Redis 存的是 rawToken，传入的也是 encryptedToken，这里需要根据实际业务调整。
        // 下面的脚本假设：Redis Key 存的值 就是 待校验的值。
        String luaScript = """
                local val = redis.call('get', KEYS[1])
                if val == ARGV[1] then
                    redis.call('del', KEYS[1])
                    return val
                end
                return nil
                """;

        try {
            // 使用 RScript.ReturnType.VALUE 直接获取字符串或 nil
            Object result = redissonClient.getScript().eval(
                    RScript.Mode.READ_WRITE,
                    luaScript,
                    RScript.ReturnType.VALUE,
                    Collections.singletonList(redisKey), // KEYS[1]
                    token                                // ARGV[1] (注意：这里需统一 Redis 存的是明文还是密文，需保持一致)
            );

            return (String) result;
        } catch (RedisException e) {
            log.error("Redis error during token validation", e);
            return null;
        }
    }

    /**
     * 判断 Token 字符串是否无效
     *
     * @param token Token 字符串
     * @return 是否无效
     */
    private boolean isInvalidToken(String token) {
        return StringUtils.isBlank(token)
                || INVALID_VALUE_NULL.equalsIgnoreCase(token)
                || INVALID_VALUE_UNDEFINED.equalsIgnoreCase(token);
    }

    /**
     * 发送 401 响应
     *
     * @param response 响应对象
     * @param message  响应消息
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        log.warn("Authorization failed: {}", message);
        // 使用 sendError 是标准做法，容器会处理错误页面。
        // 如果需要返回 JSON，可以使用 response.getWriter().write(JSON.toJSONString(...))
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
    }
}
