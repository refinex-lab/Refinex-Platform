package cn.refinex.base.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import lombok.experimental.UtilityClass;
import org.hibernate.validator.HibernateValidator;

import java.util.Set;

/**
 * 实体参数校验工具类
 * <p>
 * 基于 Hibernate Validator 实现，支持 JSR-303/JSR-380 注解校验。
 * 配置为 Fail Fast 模式（遇到第一个错误即返回）。
 *
 * @author refinex
 */
@UtilityClass
public class ValidationUtils {

    /**
     * 校验器实例（Fail Fast 配置）
     */
    private static final Validator VALIDATOR = Validation.byProvider(HibernateValidator.class)
            .configure()
            .failFast(true) // 快速失败，只要有一个参数校验失败就立即返回
            .buildValidatorFactory()
            .getValidator();

    /**
     * 校验对象
     * <p>
     * 如果校验不通过，抛出包含具体错误信息的 ValidationException
     *
     * @param object 待校验的对象
     * @param groups 校验组
     * @throws ValidationException 当参数校验不通过时抛出
     */
    public static void validate(Object object, Class<?>... groups) {
        if (object == null) {
            return;
        }

        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(object, groups);

        if (!violations.isEmpty()) {
            // 由于配置了 failFast(true)，集合中只会有一个元素，直接获取即可
            ConstraintViolation<Object> violation = violations.iterator().next();
            throw new ValidationException(violation.getMessage());
        }
    }
}
