package cn.refinex.base.validator.oneof;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 校验值必须是指定字符串数组中的一个
 * <p>
 * 使用示例:
 * <pre>{@code
 *     // 只能传 "VIP" 或 "NORMAL"
 *     @OneOf(value = {"VIP", "NORMAL"}, message = "用户类型错误")
 *     private String userType;
 * }</pre>
 *
 * @author refinex
 */
@Documented
@Constraint(validatedBy = OneOfValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface OneOf {

    /**
     * 默认错误消息
     */
    String message() default "参数值不合法";

    /**
     * 默认分组
     */
    Class<?>[] groups() default {};

    /**
     * 默认负载
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * 允许的字符串值数组
     */
    String[] value();
}
