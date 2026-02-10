package cn.refinex.base.validator.oneof;

import cn.hutool.core.text.CharSequenceUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 校验值必须是指定字符串数组中的一个
 *
 * @author refinex
 */
public class OneOfValidator implements ConstraintValidator<OneOf, Object> {

    /**
     * 允许的值
     */
    private Set<String> allowedValues;

    /**
     * 初始化允许的值集合
     *
     * @param constraintAnnotation 注解
     */
    @Override
    public void initialize(OneOf constraintAnnotation) {
        // 初始化时将数组转为 Set 提高查询效率
        allowedValues = Arrays.stream(constraintAnnotation.value())
                .collect(Collectors.toSet());
    }

    /**
     * 校验值是否在允许的值集合中
     *
     * @param value   待校验的值
     * @param context 校验器上下文
     * @return 是否在允许的值集合中
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        // 将输入值转为 String 后比较（兼容 Integer/String）
        String strVal = String.valueOf(value);
        if (CharSequenceUtil.isBlank(strVal)) {
            return true;
        }
        return allowedValues.contains(strVal);
    }
}
