package cn.refinex.ai.interfaces.vo;

import lombok.Data;

/**
 * 对话列表 VO
 *
 * @author refinex
 */
@Data
public class ConversationVO {

    /**
     * 会话唯一标识(UUID)
     */
    private String conversationId;

    /**
     * 对话标题
     */
    private String title;

    /**
     * 使用的模型ID
     */
    private Long modelId;

    /**
     * 是否置顶 1是 0否
     */
    private Integer pinned;

    /**
     * 状态 1进行中 2已归档
     */
    private Integer status;

    /**
     * 创建时间
     */
    private String gmtCreate;

    /**
     * 修改时间
     */
    private String gmtModified;
}
