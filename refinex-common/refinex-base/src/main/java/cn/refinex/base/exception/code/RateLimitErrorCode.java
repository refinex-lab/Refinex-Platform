package cn.refinex.base.exception.code;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * 限流错误码
 * <p>
 * 用于标识请求被限流的情况
 *
 * @author refinex
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public enum RateLimitErrorCode implements ErrorCode {

    REQUEST_LIMITED("REQUEST_LIMITED", "请求被限流");

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    @Override
    public String getCode() {
        return this.code;
    }

    /**
     * 获取错误消息
     *
     * @return 错误消息
     */
    @Override
    public String getMessage() {
        return this.message;
    }
}
