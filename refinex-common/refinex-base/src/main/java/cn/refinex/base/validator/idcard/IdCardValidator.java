package cn.refinex.base.validator.idcard;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdcardUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 身份证校验器
 * <p>
 * 使用 Hutool 的 IdcardUtil，支持 15 位和 18 位身份证及校验位计算。
 *
 * @author refinex
 */
public class IdCardValidator implements ConstraintValidator<IdCard, String> {

    /**
     * 是否校验身份证
     *
     * @param value   待校验值
     * @param context 校验器上下文
     * @return 是否校验成功
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (CharSequenceUtil.isBlank(value)) {
            return true;
        }
        return IdcardUtil.isValidCard(value);
    }
}
