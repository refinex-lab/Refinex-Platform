package cn.refinex.base.validator.enumvalue;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 指定枚举值校验
 * <p>
 * 校验参数值是否在指定枚举类的有效范围内。
 * <p>
 * 使用示例:
 * <pre>{@code
 *     // 必须是：MALE, FEMALE (假设 UserGender 枚举有 getCode 方法返回 1/2)
 *     // 假设前端传的是 Integer 1 或 2
 *     @EnumValue(enumClass = UserGender.class, enumMethod = "getCode", message = "性别类型错误")
 *     private Integer gender;
 * }</pre>
 *
 * @author refinex
 */
@Documented
@Constraint(validatedBy = EnumValueValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValue {

    /**
     * 默认错误消息
     */
    String message() default "参数值不在合法范围内";

    /**
     * 默认分组
     */
    Class<?>[] groups() default {};

    /**
     * 默认负载
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * 指定枚举类
     */
    Class<? extends Enum<?>> enumClass();

    /**
     * 指定判断的方法名（默认 invoke 枚举的 name() 方法，也可指定 code/getValue 等）
     */
    String enumMethod() default "name";
}
