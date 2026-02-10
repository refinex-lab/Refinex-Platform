package cn.refinex.web.handler;

import cn.refinex.base.exception.BizException;
import cn.refinex.base.exception.SystemException;
import cn.refinex.base.response.code.ResponseCode;
import cn.refinex.web.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

/**
 * 全局异常处理器
 * <p>
 * 统一拦截 Controller 层抛出的异常，并封装为标准 {@link Result} 格式返回。
 *
 * @author refinex
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 拦截参数校验异常 (JSR-303 / @Valid)
     * <p>
     * HTTP 状态码设为 400 (Bad Request)，响应体包含具体参数错误信息。
     *
     * @param ex 参数校验异常
     * @return 包含错误信息的统一响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException ex) {
        // 打印 WARN 日志，不需要堆栈，因为这是用户输入错误
        log.warn("Parameter validation failed: {}", ex.getMessage());

        // 获取绑定结果
        BindingResult bindingResult = ex.getBindingResult();

        // 提取第一条错误信息（通常前端只需要显示一个错误即可）
        FieldError firstError = bindingResult.getFieldError();
        String errorMessage = Optional.ofNullable(firstError)
                // 格式：username 不能为空
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .orElse("Invalid Parameter");

        // 返回统一的 ILLEGAL_ARGUMENT 错误码
        return Result.error(ResponseCode.ILLEGAL_ARGUMENT.name(), errorMessage);
    }

    /**
     * 拦截自定义业务异常
     * <p>
     * HTTP 状态码设为 200 (OK)，由 Result 中的 code 标识具体的业务错误。
     *
     * @param ex 业务异常
     * @return 包含业务错误码的统一响应
     */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBizException(BizException ex) {
        // 业务异常属于预期内错误，使用 WARN 级别，视情况决定是否打印堆栈
        log.warn("Business exception occurred: [{}] {}", ex.getErrorCode().getCode(), ex.getMessage());

        // 使用异常中携带的错误码和消息构建响应
        return Result.error(ex.getErrorCode().getCode(), ex.getMessage());
    }

    /**
     * 拦截自定义系统异常
     * <p>
     * 系统异常通常意味着代码 bug 或环境故障，需要打印 ERROR 日志并报警。
     *
     * @param ex 系统异常
     * @return 包含系统错误码的统一响应
     */
    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.OK) // 保持 200，让前端通过 code 判断，也可以根据规范改为 500
    public Result<Void> handleSystemException(SystemException ex) {
        // 系统异常必须打印 ERROR 和完整堆栈，以便排查
        log.error("System exception occurred!", ex);

        return Result.error(ex.getErrorCode().getCode(), ex.getMessage());
    }

    /**
     * 兜底异常处理 (拦截所有未知的 Throwable)
     * <p>
     * 类似于 catch (Exception e)，防止将原始堆栈直接暴露给前端。
     *
     * @param ex 未知异常
     * @return 统一的系统繁忙响应
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 未知错误建议返回 500
    public Result<Void> handleThrowable(Throwable ex) {
        // 这是一个未被捕获的意外异常，必须打印完整堆栈
        log.error("Unhandled exception occurred!", ex);

        // 生产环境不要返回 e.getMessage()，因为它可能包含敏感信息（如 SQL 语句）
        // 统一返回友好的提示语
        return Result.error(ResponseCode.SYSTEM_ERROR.name(), "哎呀，系统开小差了，请稍后再试~");
    }
}
