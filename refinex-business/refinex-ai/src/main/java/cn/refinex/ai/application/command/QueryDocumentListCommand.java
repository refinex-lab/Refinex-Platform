package cn.refinex.ai.application.command;

import lombok.Data;

/**
 * 查询文档列表命令
 *
 * @author refinex
 */
@Data
public class QueryDocumentListCommand {

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 目录ID(0为根目录)
     */
    private Long folderId;

    /**
     * 状态 1正常 0禁用
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
