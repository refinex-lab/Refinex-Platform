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

    PROMPT_TEMPLATE_NOT_FOUND("AI_404_PROMPT_TEMPLATE", "Prompt模板不存在"),
    PROMPT_TEMPLATE_CODE_DUPLICATED("AI_409_PROMPT_TEMPLATE_CODE", "Prompt模板编码已存在"),

    MODEL_PROVISION_NOT_FOUND("AI_404_MODEL_PROVISION", "租户模型开通配置不存在"),
    MODEL_PROVISION_DUPLICATED("AI_409_MODEL_PROVISION", "该租户已开通此模型"),

    TOOL_NOT_FOUND("AI_404_TOOL", "工具不存在"),
    TOOL_CODE_DUPLICATED("AI_409_TOOL_CODE", "工具编码已存在"),

    MCP_SERVER_NOT_FOUND("AI_404_MCP_SERVER", "MCP服务器不存在"),
    MCP_SERVER_CODE_DUPLICATED("AI_409_MCP_SERVER_CODE", "MCP服务器编码已存在"),

    SKILL_NOT_FOUND("AI_404_SKILL", "技能不存在"),
    SKILL_CODE_DUPLICATED("AI_409_SKILL_CODE", "技能编码已存在"),

    MODEL_PROVISION_DISABLED("AI_403_MODEL_PROVISION", "该模型配置已停用"),
    PROVIDER_DISABLED("AI_403_PROVIDER", "该供应商已停用"),
    MODEL_DISABLED("AI_403_MODEL", "该模型已停用"),
    API_KEY_MISSING("AI_400_API_KEY", "API Key未配置"),
    UNSUPPORTED_PROTOCOL("AI_400_PROTOCOL", "不支持的接口协议"),
    DEFAULT_MODEL_NOT_CONFIGURED("AI_404_DEFAULT_MODEL", "该租户未配置默认模型"),

    CONVERSATION_NOT_FOUND("AI_404_CONVERSATION", "对话不存在"),
    CONVERSATION_NOT_OWNED("AI_403_CONVERSATION", "无权访问该对话"),
    CHAT_STREAM_ERROR("AI_500_CHAT_STREAM", "对话流式响应异常"),
    PROMPT_TEMPLATE_RENDER_ERROR("AI_500_PROMPT_RENDER", "Prompt模板渲染失败"),
    PREFIX_CONTINUE_NO_HISTORY("AI_400_PREFIX_CONTINUE", "无可续写的历史消息"),
    ;

    private final String code;
    private final String message;
}
