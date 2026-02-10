package cn.refinex.base.validator.mobile;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 手机号校验逻辑实现
 * <p>
 * 依赖 Hutool 的 {@link Validator#isMobile(CharSequence)} 进行正则匹配。
 *
 * @author refinex
 */
public class MobileValidator implements ConstraintValidator<IsMobile, String> {

    /**
     * 初始化
     *
     * @param constraintAnnotation 注解
     */
    @Override
    public void initialize(IsMobile constraintAnnotation) {
        // 如果注解中有自定义参数（如是否支持国际号码），可在此处初始化
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    /**
     * 执行校验
     *
     * @param value   待校验的手机号
     * @param context 校验上下文
     * @return 校验是否通过
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 标准行为：如果值为 null 或空字符串，视为校验通过。
        // 必填性校验应配合 @NotBlank 或 @NotNull 使用。
        if (CharSequenceUtil.isBlank(value)) {
            return true;
        }

        // 调用 Hutool 的校验逻辑
        return Validator.isMobile(value);
    }
}
