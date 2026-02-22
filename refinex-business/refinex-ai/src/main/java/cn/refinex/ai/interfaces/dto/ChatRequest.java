package cn.refinex.ai.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 对话请求
 *
 * @author refinex
 */
@Data
public class ChatRequest {

    /**
     * 会话唯一标识(为空则新建对话)
     */
    private String conversationId;

    /**
     * 用户消息
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 模型ID(可选，为空用对话已有模型或租户默认)
     */
    private Long modelId;

    /**
     * Prompt模板ID(可选，仅新建对话时生效)
     */
    private Long promptTemplateId;

    /**
     * 模板变量
     */
    private Map<String, String> templateVariables;

    /**
     * 系统提示词(直传，优先级低于 promptTemplateId)
     */
    private String systemPrompt;
}
