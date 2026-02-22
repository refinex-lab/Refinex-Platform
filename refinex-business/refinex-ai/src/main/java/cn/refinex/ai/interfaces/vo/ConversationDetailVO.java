package cn.refinex.ai.interfaces.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 对话详情 VO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConversationDetailVO extends ConversationVO {

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 扩展信息
     */
    private String extJson;

    /**
     * 消息历史
     */
    private List<ChatMessageVO> messages;
}
