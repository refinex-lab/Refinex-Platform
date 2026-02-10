package cn.refinex.base.exception;

import cn.refinex.base.exception.code.ErrorCode;
import lombok.Getter;

/**
 * 业务逻辑异常
 * <p>
 * 处理业务流程中的校验失败、规则冲突等预期内的异常情况。建议在 Service 层抛出，由统一异常处理器拦截并返回给前端。
 *
 * @author refinex
 */
@Getter
public class BizException extends RuntimeException {

    /**
     * 错误码对象
     */
    private final ErrorCode errorCode;

    /**
     * 根据错误码构造业务逻辑异常
     *
     * @param errorCode 错误码
     */
    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 自定义消息构造业务逻辑异常
     *
     * @param message   自定义错误消息
     * @param errorCode 错误码
     */
    public BizException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 完整构造业务逻辑异常
     *
     * @param message   自定义错误消息
     * @param cause     异常原因
     * @param errorCode 错误码
     */
    public BizException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 异常链构造业务逻辑异常
     *
     * @param cause     异常原因
     * @param errorCode 错误码
     */
    public BizException(Throwable cause, ErrorCode errorCode) {
        super(cause == null ? null : cause.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
