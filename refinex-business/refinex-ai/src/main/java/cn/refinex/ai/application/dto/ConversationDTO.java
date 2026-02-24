package cn.refinex.ai.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话 DTO
 *
 * @author refinex
 */
@Data
public class ConversationDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 会话唯一标识(UUID，关联 Spring AI ChatMemory)
     */
    private String conversationId;

    /**
     * 组织ID
     */
    private Long estabId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 对话标题(可由 AI 自动生成)
     */
    private String title;

    /**
     * 使用的模型ID
     */
    private Long modelId;

    /**
     * 系统提示词(本次对话的 system message)
     */
    private String systemPrompt;

    /**
     * 是否置顶 1是 0否
     */
    private Integer pinned;

    /**
     * 状态 1进行中 2已归档
     */
    private Integer status;

    /**
     * 扩展信息(如温度、top_p等对话级参数覆盖)
     */
    private String extJson;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;

    /**
     * 修改时间
     */
    private LocalDateTime gmtModified;
}
