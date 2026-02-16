package cn.refinex.auth.web.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

/**
 * Web 请求工具
 *
 * @author refinex
 */
@UtilityClass
public final class RequestUtils {

    /**
     * 获取客户端 IP
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String ip = header(request, "X-Forwarded-For");
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        if (isUnknown(ip)) {
            ip = header(request, "X-Real-IP");
        }
        if (isUnknown(ip)) {
            ip = header(request, "Proxy-Client-IP");
        }
        if (isUnknown(ip)) {
            ip = header(request, "WL-Proxy-Client-IP");
        }
        if (isUnknown(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取用户代理
     *
     * @param request HTTP 请求
     * @return 用户代理
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request == null ? null : request.getHeader("User-Agent");
    }

    /**
     * 获取请求头
     *
     * @param request HTTP 请求
     * @param name    请求头名称
     * @return 请求头值
     */
    private static String header(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value == null || value.isBlank() ? null : value;
    }

    /**
     * 判断是否未知
     *
     * @param value 值
     * @return 是否未知
     */
    private static boolean isUnknown(String value) {
        return value == null || value.isBlank() || "unknown".equalsIgnoreCase(value);
    }
}
