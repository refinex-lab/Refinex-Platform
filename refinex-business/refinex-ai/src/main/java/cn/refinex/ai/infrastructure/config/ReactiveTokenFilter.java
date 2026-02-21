package cn.refinex.ai.infrastructure.config;

import cn.dev33.satoken.stp.StpUtil;
import cn.refinex.web.autoconfigure.RefinexWebProperties;
import cn.refinex.web.context.UserContext;
import cn.refinex.web.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * WebFlux 版 Token 校验过滤器
 * <p>
 * 功能与 {@code cn.refinex.web.filter.TokenFilter}（Servlet 版）完全对齐：
 * <ol>
 *   <li>拦截 HTTP 请求，提取 Authorization / Sa-Token 头</li>
 *   <li>白名单路径直接放行</li>
 *   <li>验证 Token 合法性（防重放 + Sa-Token 登录态）</li>
 *   <li>将验证通过的 Token 写入 {@link UserContext}</li>
 * </ol>
 * <p>
 * 差异点：
 * <ul>
 *   <li>实现 {@link WebFilter} 而非 Servlet {@code Filter}</li>
 *   <li>阻塞操作（Redis Lua、Sa-Token 查询）通过 {@code subscribeOn(boundedElastic)} 调度</li>
 *   <li>UserContext 基于 ThreadLocal，在 boundedElastic 线程上设置/清理</li>
 * </ul>
 *
 * @author refinex
 */
@Slf4j
@Order(1)
public class ReactiveTokenFilter implements WebFilter {

    private static final String HEADER_AUTH = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_STRESS = "isStress";
    private static final String INVALID_VALUE_NULL = "null";
    private static final String INVALID_VALUE_UNDEFINED = "undefined";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final RedissonClient redissonClient;
    private final RefinexWebProperties webProperties;

    @Value("${refinex.security.stress-mode-enabled:false}")
    private boolean stressModeEnabled;

    @Value("${sa-token.token-name:Refinex-Token}")
    private String saTokenHeaderName;

    /**
     * 构造函数注入
     *
     * @param redissonClient Redisson 客户端
     * @param webProperties  Web 配置
     */
    public ReactiveTokenFilter(RedissonClient redissonClient, RefinexWebProperties webProperties) {
        this.redissonClient = redissonClient;
        this.webProperties = webProperties;
    }

    /**
     * 过滤器逻辑
     */
    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 白名单路径直接放行
        if (isExcludedPath(request)) {
            return chain.filter(exchange);
        }

        // 从请求头中解析 Token
        String token = resolveToken(request);
        // 解析是否为压测 Token
        boolean isStress = stressModeEnabled && BooleanUtils.toBoolean(request.getHeaders().getFirst(HEADER_STRESS));

        // 基础非空校验（压测 Token 不校验）
        if (isInvalidToken(token) && !isStress) {
            return sendUnauthorized(exchange);
        }

        // 阻塞校验逻辑调度到 boundedElastic 线程
        return Mono.fromCallable(() -> validateAndConsumeToken(token, isStress))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(validToken -> {
                    // 在 boundedElastic 线程上设置 UserContext，后续阻塞操作也在此线程
                    UserContext.setToken(validToken);
                    UserContext.setStressTest(isStress);
                    // 继续FilterChain，后续操作在 boundedElastic 线程
                    return chain.filter(exchange).doFinally(signal -> UserContext.clear());
                })
                .onErrorResume(e -> {
                    log.error("ReactiveTokenFilter internal error", e);
                    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    return exchange.getResponse().setComplete();
                });
    }

    /**
     * 校验并消费 Token，逻辑与 Servlet 版完全一致
     *
     * @param token    token
     * @param isStress 是否为压测 Token
     * @return 校验成功返回 token，否则返回 null
     */
    private String validateAndConsumeToken(String token, boolean isStress) {
        // 压测 Token 直接返回随机 UUID
        if (isStress) {
            return UUID.randomUUID().toString();
        }

        // 非压测 Token 从 Redis 校验
        String redisKey = TokenUtils.parseToken(token);
        if (redisKey == null) {
            // 解析失败，按 Sa-Token 校验
            return validateSaToken(token);
        }

        // 从 Redis 校验 Token 是否匹配
        String luaScript = """
                local val = redis.call('get', KEYS[1])
                if val == ARGV[1] then
                    redis.call('del', KEYS[1])
                    return val
                end
                return nil
                """;

        try {
            // 执行 Lua 脚本校验 Token
            Object result = redissonClient.getScript().eval(
                    RScript.Mode.READ_WRITE,
                    luaScript,
                    RScript.ReturnType.VALUE,
                    Collections.singletonList(redisKey),
                    token
            );
            return (String) result;
        } catch (RedisException e) {
            log.error("Redis error during token validation", e);
            return null;
        }
    }

    /**
     * 按 Sa-Token 登录态校验 Token
     *
     * @param token token
     * @return 校验成功返回 token，否则返回 null
     */
    private String validateSaToken(String token) {
        try {
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                log.warn("Sa-Token validation failed: token not found");
                return null;
            }
            return token;
        } catch (Exception e) {
            log.warn("Sa-Token validation failed: {}", token, e);
            return null;
        }
    }

    /**
     * 从请求头中解析 Token
     * <p>
     * 按以下优先级从请求头中获取 Token：
     * 1. 首先尝试从标准 Authorization 请求头中获取 Token
     * 2. 如果标准请求头中没有 Token，则尝试从自定义的 saTokenHeaderName 请求头中获取
     * 3. 如果 saTokenHeaderName 为空或与标准请求头名称相同，则直接返回第一步的结果
     * </p>
     *
     * @param request HTTP 请求对象，用于获取请求头信息
     * @return 解析后的 Token 字符串，如果未找到有效的 Token 则返回 null
     */
    private String resolveToken(ServerHttpRequest request) {
        String token = normalizeToken(request.getHeaders().getFirst(HEADER_AUTH));
        if (StringUtils.isNotBlank(token)) {
            return token;
        }

        if (StringUtils.isBlank(saTokenHeaderName) || HEADER_AUTH.equalsIgnoreCase(saTokenHeaderName)) {
            return token;
        }

        return normalizeToken(request.getHeaders().getFirst(saTokenHeaderName));
    }

    /**
     * 标准化 Token，兼容 Bearer 前缀
     *
     * @param token 原始 Token
     * @return 标准化后的 Token
     */
    private String normalizeToken(String token) {
        if (StringUtils.isBlank(token)) {
            return token;
        }

        String normalized = token.trim();
        if (Strings.CI.startsWith(normalized, BEARER_PREFIX)) {
            return normalized.substring(BEARER_PREFIX.length()).trim();
        }
        return normalized;
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
     * 是否命中免鉴权路径
     *
     * @param request 请求
     * @return 是否命中免鉴权路径
     */
    private boolean isExcludedPath(ServerHttpRequest request) {
        List<String> excludeUrls = webProperties.getExcludeUrls();
        if (CollectionUtils.isEmpty(excludeUrls)) {
            return false;
        }

        String path = request.getPath().value();
        for (String pattern : excludeUrls) {
            if (StringUtils.isNotBlank(pattern) && PATH_MATCHER.match(pattern.trim(), path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 发送 401 响应
     *
     * @param exchange 交换对象
     * @return 响应 Mono
     */
    private Mono<Void> sendUnauthorized(ServerWebExchange exchange) {
        log.warn("Authorization failed: {}", "Token is missing or invalid format");
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}