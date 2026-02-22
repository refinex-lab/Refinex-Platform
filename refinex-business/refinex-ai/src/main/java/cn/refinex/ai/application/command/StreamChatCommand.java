package cn.refinex.ai.application.command;

import lombok.Data;

import java.util.Map;

/**
 * 流式对话命令
 *
 * @author refinex
 */
@Data
public class StreamChatCommand {

    /**
     * 会话唯一标识(为空则新建对话)
     */
    private String conversationId;

    /**
     * 用户消息
     */
    private String message;

    /**
     * 系统提示词(直传，优先级低于 promptTemplateId)
     */
    private String systemPrompt;

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
     * 组织ID(Controller 从 LoginUserHelper 注入)
     */
    private Long estabId;

    /**
     * 用户ID(Controller 从 LoginUserHelper 注入)
     */
    private Long userId;
}
