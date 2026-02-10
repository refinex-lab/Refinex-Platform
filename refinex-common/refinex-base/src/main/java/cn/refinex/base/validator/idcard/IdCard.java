package cn.refinex.base.validator.idcard;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 身份证校验注解
 * <p>
 * 使用示例:
 * <pre>{@code
 *     // 选填，但填了必须是合法身份证号码
 *     @IdCard
 *     private String idCard;
 * }</pre>
 *
 * @author refinex
 */
@Documented
@Constraint(validatedBy = IdCardValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface IdCard {

    /**
     * 错误消息
     */
    String message() default "身份证号码格式不正确";

    /**
     * 组
     */
    Class<?>[] groups() default {};

    /**
     * 负载
     */
    Class<? extends Payload>[] payload() default {};
}
