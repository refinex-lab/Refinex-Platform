package cn.refinex.ai.infrastructure.config;

import cn.dev33.satoken.stp.StpUtil;
import cn.refinex.api.user.model.context.LoginUser;
import cn.refinex.satoken.helper.LoginUserHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * WebFlux 版登录用户解析过滤器
 * <p>
 * 职责单一：从请求头提取 Sa-Token → 解析 loginId → 获取 LoginUser → 存入 Exchange 属性。
 * <p>
 * 鉴权（是否登录、是否有权限）由 Gateway 的 {@code SaReactorFilter} 统一完成，
 * 本过滤器仅负责在 WebFlux 下游服务中恢复用户上下文。
 * <p>
 * Sa-Token 1.44.0 兼容说明：
 * <ul>
 *   <li>{@code SaReactorFilter} 在 finally 中提前清除了 SaTokenContext，
 *       导致跳线程后 {@code StpUtil.getSession()} 等方法不可用
 *       （参见 <a href="https://github.com/dromara/Sa-Token/issues/846">Sa-Token #846</a>）</li>
 *   <li>解决方案：通过 {@code StpUtil.getLoginIdByToken(token)}（不依赖 SaTokenContext）
 *       获取 loginId，再通过 {@code LoginUserHelper.getLoginUser(loginId)}（直接查 Redis Session）
 *       获取 {@link LoginUser}，存入 {@link ReactiveLoginUserHolder}</li>
 * </ul>
 *
 * @author refinex
 */
@Slf4j
@Order(1)
public class ReactiveTokenFilter implements WebFilter {

    private static final String HEADER_AUTH = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${sa-token.token-name:Refinex-Token}")
    private String saTokenHeaderName;

    /**
     * 过滤器逻辑：解析 Token 并将 LoginUser 存入 Exchange 属性
     *
     * @param exchange 当前请求的 ServerWebExchange
     * @param chain    过滤器链
     * @return 过滤器链执行结果
     */
    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String token = resolveToken(exchange.getRequest());
        if (StringUtils.isBlank(token)) {
            return sendUnauthorized(exchange);
        }

        // Sa-Token 查 Redis 是阻塞操作，调度到 boundedElastic 线程解析用户
        // 注意：chain.filter(exchange) 不能放在 flatMap 内部，否则会在 boundedElastic 线程上
        // 创建和订阅下游响应体 Mono，导致 Netty event loop 无法正确写出响应（请求挂住）
        // 正确做法：用 doOnNext 存属性 + then(Mono.defer(...)) 衔接 filter chain
        return Mono.fromCallable(() -> resolveLoginUser(token))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(loginUser ->
                        ReactiveLoginUserHolder.setToExchange(exchange, loginUser.getUserId(), loginUser))
                .then(Mono.defer(() -> chain.filter(exchange)))
                .onErrorResume(e -> {
                    log.warn("Failed to resolve login user from token", e);
                    return sendUnauthorized(exchange);
                });
    }

    /**
     * 通过 Sa-Token 解析登录用户
     * <p>
     * 使用 {@code StpUtil.getLoginIdByToken} 而非 {@code StpUtil.getLoginId}，
     * 避免依赖 SaTokenContext（WebFlux 跳线程后不可用）。
     *
     * @param token Sa-Token 令牌
     * @return 登录用户信息，解析失败返回 null（触发 Mono 空信号 → switchIfEmpty）
     */
    private LoginUser resolveLoginUser(String token) {
        Object loginId = StpUtil.getLoginIdByToken(token);
        if (loginId == null) {
            log.warn("Token validated by gateway but loginId is null, token may have expired between gateway and service");
            return null;
        }
        return LoginUserHelper.getLoginUser(loginId);
    }

    /**
     * 从请求头中解析 Token
     * <p>
     * 优先从 Authorization 头获取（兼容 Bearer 前缀），
     * 其次从自定义 Sa-Token 头（如 Refinex-Token）获取。
     *
     * @param request HTTP 请求
     * @return 解析后的 Token，未找到返回 null
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
     * 标准化 Token，去除 Bearer 前缀
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
     * 发送 401 未授权响应
     *
     * @param exchange 当前请求的 ServerWebExchange
     * @return 响应完成信号
     */
    private Mono<Void> sendUnauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
