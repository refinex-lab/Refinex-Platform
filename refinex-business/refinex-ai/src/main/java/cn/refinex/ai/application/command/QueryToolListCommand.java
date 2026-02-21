package cn.refinex.ai.application.command;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询工具列表命令
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryToolListCommand extends PageRequest {

    /**
     * 工具类型(FUNCTION/MCP/HTTP)
     */
    private String toolType;

    /**
     * 状态 1启用 0停用
     */
    private Integer status;

    /**
     * 关键词(匹配工具名称/编码)
     */
    private String keyword;
}
