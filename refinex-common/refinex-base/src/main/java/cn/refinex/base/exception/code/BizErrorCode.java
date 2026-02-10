package cn.refinex.base.exception.code;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * 业务错误码
 * <p>
 * 用于标识业务处理过程中的异常情况
 *
 * @author refinex
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public enum BizErrorCode implements ErrorCode {

    HTTP_CLIENT_ERROR("HTTP_CLIENT_ERROR", "HTTP 客户端错误"),
    HTTP_SERVER_ERROR("HTTP_SERVER_ERROR", "HTTP 服务端错误"),
    SEND_NOTICE_DUPLICATED("SEND_NOTICE_DUPLICATED", "不允许重复发送通知"),
    NOTICE_SAVE_FAILED("NOTICE_SAVE_FAILED", "通知保存失败"),
    STATE_MACHINE_TRANSITION_FAILED("STATE_MACHINE_TRANSITION_FAILED", "状态机转换失败"),
    DUPLICATED("DUPLICATED", "重复请求"),
    REMOTE_CALL_RESPONSE_IS_NULL("REMOTE_CALL_RESPONSE_IS_NULL", "远程调用返回结果为空"),
    REMOTE_CALL_RESPONSE_IS_FAILED("REMOTE_CALL_RESPONSE_IS_FAILED", "远程调用返回结果失败");

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
