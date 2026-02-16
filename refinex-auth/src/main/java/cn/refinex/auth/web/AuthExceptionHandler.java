package cn.refinex.auth.web;

import cn.refinex.base.exception.BizException;
import cn.refinex.base.exception.SystemException;
import cn.refinex.base.response.SingleResponse;
import cn.refinex.base.response.code.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 认证服务全局异常处理器
 *
 * @author refinex
 */
@Slf4j
@RestControllerAdvice
public class AuthExceptionHandler {

    /**
     * 处理参数验证异常
     *
     * @param ex 参数验证异常
     * @return 单一结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SingleResponse<Void> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Parameter validation failed: {}", ex.getMessage());
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .orElse("Invalid Parameter");
        return SingleResponse.fail(ResponseCode.ILLEGAL_ARGUMENT.name(), message);
    }

    /**
     * 处理业务异常
     *
     * @param ex 业务异常
     * @return 单一结果
     */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<Void> handleBizException(BizException ex) {
        log.warn("Business exception: [{}] {}", ex.getErrorCode().getCode(), ex.getMessage());
        return SingleResponse.fail(ex.getErrorCode().getCode(), ex.getMessage());
    }

    /**
     * 处理系统异常
     *
     * @param ex 系统异常
     * @return 单一结果
     */
    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.OK)
    public SingleResponse<Void> handleSystemException(SystemException ex) {
        log.error("System exception", ex);
        return SingleResponse.fail(ex.getErrorCode().getCode(), ex.getMessage());
    }

    /**
     * 处理其他异常
     *
     * @param ex 其他异常
     * @return 单一结果
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public SingleResponse<Void> handleThrowable(Throwable ex) {
        log.error("Unhandled exception", ex);
        return SingleResponse.fail(ResponseCode.SYSTEM_ERROR.name(), "系统繁忙，请稍后再试");
    }
}
