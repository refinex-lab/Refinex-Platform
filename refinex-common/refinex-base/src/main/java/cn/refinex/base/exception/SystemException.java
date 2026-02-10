package cn.refinex.base.exception;

import cn.refinex.base.exception.code.ErrorCode;
import lombok.Getter;

/**
 * 系统异常基类
 * <p>
 * 封装非业务性质的系统错误，如基础设施故障、中间件异常等。属于 RuntimeException，不强制调用方显式捕获。
 *
 * @author refinex
 */
@Getter
public class SystemException extends RuntimeException {

    /**
     * 错误码对象
     */
    private final ErrorCode errorCode;

    /**
     * 根据错误码构造系统异常
     *
     * @param errorCode 错误码
     */
    public SystemException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 自定义消息构造系统异常
     *
     * @param message   自定义错误消息
     * @param errorCode 错误码
     */
    public SystemException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 完整构造系统异常
     *
     * @param message   自定义错误消息
     * @param cause     异常原因
     * @param errorCode 错误码
     */
    public SystemException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 异常链构造系统异常
     *
     * @param cause     异常原因
     * @param errorCode 错误码
     */
    public SystemException(Throwable cause, ErrorCode errorCode) {
        super(cause == null ? null : cause.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
