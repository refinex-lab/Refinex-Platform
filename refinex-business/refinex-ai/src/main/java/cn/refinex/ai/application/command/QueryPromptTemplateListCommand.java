package cn.refinex.ai.application.command;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询Prompt模板列表命令
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryPromptTemplateListCommand extends PageRequest {

    /**
     * 状态 1启用 0停用
     */
    private Integer status;

    /**
     * 分类(如 rag/summary/translate/code_gen/chat)
     */
    private String category;

    /**
     * 关键词(匹配模板名称/编码)
     */
    private String keyword;
}
