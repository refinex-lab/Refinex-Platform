package cn.refinex.ai.infrastructure.config;

import cn.refinex.api.user.model.context.LoginUser;
import lombok.experimental.UtilityClass;
import org.springframework.web.server.ServerWebExchange;

/**
 * WebFlux 环境下的登录用户上下文持有者
 * <p>
 * 由于 Sa-Token 1.44.0 在 WebFlux 环境下存在 SaTokenContext 上下文提前清除的问题
 * （参见 <a href="https://github.com/dromara/Sa-Token/issues/846">Sa-Token #846</a>），
 * {@code StpUtil.getSession()} / {@code StpUtil.getLoginIdAsLong()} 等依赖 SaTokenContext 的方法
 * 在 {@code Mono.fromCallable(...).subscribeOn(boundedElastic)} 跳线程后不可用。
 * <p>
 * 本类在 {@link ReactiveTokenFilter} 中通过不依赖 SaTokenContext 的方式
 * （{@code StpUtil.getLoginIdByToken} + {@code LoginUserHelper.getLoginUser(loginId)}）
 * 提前解析用户信息，存入 {@code ServerWebExchange} 属性（线程安全的 ConcurrentHashMap），
 * 供 Controller 层跨线程读取。
 * <p>
 * 数据存储策略：
 * <ul>
 *   <li>主存储：{@code ServerWebExchange.getAttributes()}，跨线程安全，在 Filter 中写入</li>
 *   <li>辅助存储：ThreadLocal，在 Controller 方法中通过 {@link #initFromExchange(ServerWebExchange)}
 *       从 Exchange 属性复制到当前线程，供 {@code Mono.fromCallable} 内的 Application Service 层读取</li>
 * </ul>
 * <p>
 * 对标 refinex-user 服务中 {@code CurrentUserProvider} + {@code SaTokenCurrentUserProvider} 的能力，
 * 但完全绕过 SaTokenContext。
 *
 * @author refinex
 */
@UtilityClass
public class ReactiveLoginUserHolder {

    /** Exchange 属性 Key：登录ID */
    private static final String ATTR_LOGIN_ID = "ReactiveLoginUserHolder.loginId";
    /** Exchange 属性 Key：登录用户信息 */
    private static final String ATTR_LOGIN_USER = "ReactiveLoginUserHolder.loginUser";

    private static final ThreadLocal<Object> LOGIN_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<LoginUser> LOGIN_USER_HOLDER = new ThreadLocal<>();

    // ── Exchange 属性操作（Filter 层使用，跨线程安全） ──

    /**
     * 将登录信息写入 Exchange 属性（在 ReactiveTokenFilter 中调用）
     *
     * @param exchange  当前请求的 ServerWebExchange
     * @param loginId   登录ID
     * @param loginUser 登录用户信息
     */
    public static void setToExchange(ServerWebExchange exchange, Object loginId, LoginUser loginUser) {
        exchange.getAttributes().put(ATTR_LOGIN_ID, loginId);
        if (loginUser != null) {
            exchange.getAttributes().put(ATTR_LOGIN_USER, loginUser);
        }
    }

    /**
     * 从 Exchange 属性读取登录信息并写入当前线程的 ThreadLocal
     * <p>
     * 在 Controller 方法中调用，确保后续 {@code Mono.fromCallable} 内的
     * Application Service 层可以通过 {@link #getUserId()} / {@link #getEstabId()} 读取。
     *
     * @param exchange 当前请求的 ServerWebExchange
     */
    public static void initFromExchange(ServerWebExchange exchange) {
        Object loginId = exchange.getAttribute(ATTR_LOGIN_ID);
        LoginUser loginUser = exchange.getAttribute(ATTR_LOGIN_USER);
        LOGIN_ID_HOLDER.set(loginId);
        LOGIN_USER_HOLDER.set(loginUser);
    }

    // ── ThreadLocal 操作（Controller / Service 层使用） ──

    /**
     * 设置当前线程的登录信息（ThreadLocal）
     *
     * @param loginId   登录ID
     * @param loginUser 登录用户信息
     */
    public static void set(Object loginId, LoginUser loginUser) {
        LOGIN_ID_HOLDER.set(loginId);
        LOGIN_USER_HOLDER.set(loginUser);
    }

    /**
     * 从 ThreadLocal 获取当前登录用户ID
     *
     * @return 用户ID，未登录返回 null
     */
    public static Long getUserId() {
        Object loginId = LOGIN_ID_HOLDER.get();
        if (loginId == null) {
            return null;
        }
        if (loginId instanceof Long l) {
            return l;
        }
        try {
            return Long.parseLong(String.valueOf(loginId));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从 ThreadLocal 获取当前登录用户信息
     *
     * @return 登录用户信息，未登录返回 null
     */
    public static LoginUser getLoginUser() {
        return LOGIN_USER_HOLDER.get();
    }

    /**
     * 从 ThreadLocal 获取当前组织ID
     *
     * @return 组织ID，未登录返回 null
     */
    public static Long getEstabId() {
        LoginUser loginUser = LOGIN_USER_HOLDER.get();
        return loginUser == null ? null : loginUser.getEstabId();
    }

    /**
     * 从 ThreadLocal 获取当前团队ID
     *
     * @return 团队ID，未登录返回 null
     */
    public static Long getTeamId() {
        LoginUser loginUser = LOGIN_USER_HOLDER.get();
        return loginUser == null ? null : loginUser.getTeamId();
    }

    /**
     * 清理 ThreadLocal 上下文
     */
    public static void clear() {
        LOGIN_ID_HOLDER.remove();
        LOGIN_USER_HOLDER.remove();
    }
}
