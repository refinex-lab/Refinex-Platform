package cn.refinex.base.validator.mobile;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 手机号格式校验注解
 * <p>
 * 遵循 JSR-303 标准：
 * 1. 仅校验格式，不校验非空（null 或空字符串被视为合法，由 @NotBlank 另行约束）。
 * 2. 默认使用大陆手机号规则。
 * <p>
 * 使用示例:
 * <pre>{@code
 *     // 选填，但填了必须是合法手机号
 *     @IsMobile
 *     private String phone;
 * }</pre>
 *
 * @author refinex
 */
@Documented
@Constraint(validatedBy = MobileValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface IsMobile {

    /**
     * 校验失败时的错误提示信息
     */
    String message() default "手机号格式不正确";

    /**
     * 校验分组
     */
    Class<?>[] groups() default {};

    /**
     * 负载信息
     */
    Class<? extends Payload>[] payload() default {};
}
