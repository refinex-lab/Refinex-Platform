package cn.refinex.base.exception;

import cn.refinex.base.exception.code.ErrorCode;
import lombok.Getter;

/**
 * 远程调用异常
 * <p>
 * 当调用第三方服务（如微服务接口、外部 API）响应异常或超时时抛出。
 *
 * @author refinex
 */
@Getter
public class RemoteCallException extends SystemException {

    /**
     * 根据错误码构造远程调用异常
     *
     * @param errorCode 错误码
     */
    public RemoteCallException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 自定义消息构造远程调用异常
     *
     * @param message   自定义错误消息
     * @param errorCode 错误码
     */
    public RemoteCallException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    /**
     * 完整构造远程调用异常
     *
     * @param message   自定义错误消息
     * @param cause     异常原因
     * @param errorCode 错误码
     */
    public RemoteCallException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause, errorCode);
    }

    /**
     * 异常链构造远程调用异常
     *
     * @param cause     异常原因
     * @param errorCode 错误码
     */
    public RemoteCallException(Throwable cause, ErrorCode errorCode) {
        super(cause, errorCode);
    }
}
