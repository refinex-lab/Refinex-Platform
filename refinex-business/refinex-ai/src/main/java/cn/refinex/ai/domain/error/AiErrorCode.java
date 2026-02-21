package cn.refinex.ai.domain.error;

import cn.refinex.base.exception.code.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 模块错误码
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum AiErrorCode implements ErrorCode {

    INVALID_PARAM("AI_400", "参数错误"),

    PROVIDER_NOT_FOUND("AI_404_PROVIDER", "供应商不存在"),
    PROVIDER_CODE_DUPLICATED("AI_409_PROVIDER_CODE", "供应商编码已存在"),

    MODEL_NOT_FOUND("AI_404_MODEL", "模型不存在"),
    MODEL_CODE_DUPLICATED("AI_409_MODEL_CODE", "模型编码已存在"),
    ;

    private final String code;
    private final String message;
}
