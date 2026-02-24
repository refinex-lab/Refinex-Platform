package cn.refinex.ai.interfaces.dto;

import lombok.Data;

/**
 * 对话列表查询参数
 *
 * @author refinex
 */
@Data
public class ConversationListQuery {

    /**
     * 当前页码
     */
    private Integer currentPage;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 状态 1进行中 2已归档
     */
    private Integer status;
}
