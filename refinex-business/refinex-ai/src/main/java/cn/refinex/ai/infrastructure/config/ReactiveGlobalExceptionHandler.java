package cn.refinex.ai.infrastructure.config;

import cn.refinex.base.exception.BizException;
import cn.refinex.base.exception.SystemException;
import cn.refinex.base.response.code.ResponseCode;
import cn.refinex.web.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.Optional;

/**
 * WebFlux 版全局异常处理器
 * <p>
 * 功能与 {@code cn.refinex.web.handler.GlobalExceptionHandler}（Servlet 版）完全对齐。
 * <p>
 * 差异点：
 * <ul>
 *   <li>参数校验异常类型为 {@link WebExchangeBindException}（WebFlux），
 *       而非 {@code MethodArgumentNotValidException}（Servlet MVC）</li>
 *   <li>{@code @RestControllerAdvice} 在 WebFlux 中同样生效</li>
 * </ul>
 *
 * @author refinex
 */
@Slf4j
@RestControllerAdvice
public class ReactiveGlobalExceptionHandler {

    /**
     * 拦截参数校验异常（WebFlux 版 @Valid 校验失败）
     */
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(WebExchangeBindException ex) {
        log.warn("Parameter validation failed: {}", ex.getMessage());

        BindingResult bindingResult = ex.getBindingResult();
        FieldError firstError = bindingResult.getFieldError();
        String errorMessage = Optional.ofNullable(firstError)
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .orElse("Invalid Parameter");

        return Result.error(ResponseCode.ILLEGAL_ARGUMENT.name(), errorMessage);
    }

    /**
     * 拦截自定义业务异常
     */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBizException(BizException ex) {
        log.warn("Business exception occurred: [{}] {}", ex.getErrorCode().getCode(), ex.getMessage());
        return Result.error(ex.getErrorCode().getCode(), ex.getMessage());
    }

    /**
     * 拦截自定义系统异常
     */
    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleSystemException(SystemException ex) {
        log.error("System exception occurred!", ex);
        return Result.error(ex.getErrorCode().getCode(), ex.getMessage());
    }

    /**
     * 兜底异常处理
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleThrowable(Throwable ex) {
        log.error("Unhandled exception occurred!", ex);
        return Result.error(ResponseCode.SYSTEM_ERROR.name(), "系统繁忙，请稍后再试");
    }
}
