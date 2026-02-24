package cn.refinex.ai.application.command;

import lombok.Data;

/**
 * 对话列表查询命令
 *
 * @author refinex
 */
@Data
public class QueryConversationListCommand {

    /**
     * 组织ID
     */
    private Long estabId;

    /**
     * 用户ID
     */
    private Long userId;

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
