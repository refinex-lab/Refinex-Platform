package cn.refinex.base.validator.enumvalue;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 枚举值校验器
 *
 * @author refinex
 */
@Slf4j
public class EnumValueValidator implements ConstraintValidator<EnumValue, Object> {

    /**
     * 枚举类
     */
    private Class<? extends Enum<?>> enumClass;

    /**
     * 枚举方法
     */
    private String enumMethod;

    /**
     * 缓存枚举值的合法集合，避免每次校验都反射
     * Key: EnumClass_MethodName, Value: Set<ValidValues>
     */
    private static final ConcurrentHashMap<String, Set<Object>> ENUM_VALUE_CACHE = new ConcurrentHashMap<>();

    /**
     * 初始化枚举类和方法
     *
     * @param constraintAnnotation 枚举值校验注解
     */
    @Override
    public void initialize(EnumValue constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
        this.enumMethod = constraintAnnotation.enumMethod();
    }

    /**
     * 校验值是否在枚举值范围内
     *
     * @param value        待校验值
     * @param context      校验器上下文
     * @return 是否合法
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // 同样遵循不判空原则
        }

        if (value instanceof String str && (str).isEmpty()) {
            return true;
        }

        // 获取缓存 Key
        String cacheKey = enumClass.getName() + "_" + enumMethod;

        // 双重检查锁或 computeIfAbsent 均可，这里用 computeIfAbsent 简化
        Set<Object> validValues = ENUM_VALUE_CACHE.computeIfAbsent(cacheKey, k -> {
            Set<Object> values = new HashSet<>();
            try {
                Enum<?>[] enums = enumClass.getEnumConstants();
                // 如果是 name() 方法，直接拿，否则反射调用
                if ("name".equals(enumMethod)) {
                    for (Enum<?> e : enums) {
                        values.add(e.name());
                    }
                } else {
                    Method method = enumClass.getMethod(enumMethod);
                    for (Enum<?> e : enums) {
                        Object result = method.invoke(e);
                        values.add(result);
                    }
                }
            } catch (Exception e) {
                log.error("EnumValue validator init failed", e);
            }
            return values;
        });

        // 校验值是否存在（注意类型匹配，如 Integer vs Long）
        // 建议转 String 比较以兼容不同数值类型，或者保持严格类型
        return validValues.contains(value);
    }
}
