package cn.refinex.ai.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 向量化状态枚举
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum VectorStatus {

    NOT_VECTORIZED(0, "未向量化"),
    VECTORIZING(1, "向量化中"),
    COMPLETED(2, "已完成"),
    FAILED(3, "失败");

    private final int code;
    private final String description;
}
