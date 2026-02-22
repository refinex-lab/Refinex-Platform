package cn.refinex.ai.interfaces.vo;

import lombok.Data;

/**
 * 聊天消息 VO
 *
 * @author refinex
 */
@Data
public class ChatMessageVO {

    /**
     * 消息类型(USER/ASSISTANT/SYSTEM)
     */
    private String type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息时间
     */
    private String timestamp;

    /**
     * 推理内容(仅推理模型的 ASSISTANT 消息)
     */
    private String reasoningContent;
}
