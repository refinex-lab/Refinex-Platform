package cn.refinex.auth.domain.model;

import lombok.experimental.UtilityClass;

/**
 * 登录日志上下文
 *
 * @author refinex
 */
@UtilityClass
public class LoginLogContextHolder {

    /**
     * 登录日志事件
     */
    private static final ThreadLocal<LoginLogEvent> HOLDER = new ThreadLocal<>();

    /**
     * 设置登录日志事件
     *
     * @param event 登录日志事件
     */
    public static void set(LoginLogEvent event) {
        if (event != null) {
            HOLDER.set(event);
        }
    }

    /**
     * 获取并移除登录日志事件
     *
     * @return 登录日志事件
     */
    public static LoginLogEvent take() {
        LoginLogEvent event = HOLDER.get();
        HOLDER.remove();
        return event;
    }

    /**
     * 清除登录日志事件
     */
    public static void clear() {
        HOLDER.remove();
    }
}
