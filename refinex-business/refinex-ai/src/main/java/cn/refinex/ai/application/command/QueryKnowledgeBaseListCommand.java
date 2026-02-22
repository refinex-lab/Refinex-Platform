package cn.refinex.ai.application.command;

import lombok.Data;

/**
 * 查询知识库列表命令
 *
 * @author refinex
 */
@Data
public class QueryKnowledgeBaseListCommand {

    /**
     * 状态 1启用 0停用
     */
    private Integer status;

    /**
     * 关键字
     */
    private String keyword;

    /**
     * 当前页码
     */
    private Integer currentPage;

    /**
     * 每页数量
     */
    private Integer pageSize;
}
