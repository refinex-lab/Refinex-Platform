package cn.refinex.ai.application.command;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询MCP服务器列表命令
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryMcpServerListCommand extends PageRequest {

    /**
     * 传输类型(stdio/sse)
     */
    private String transportType;

    /**
     * 状态 1启用 0停用
     */
    private Integer status;

    /**
     * 关键词(匹配服务器名称/编码)
     */
    private String keyword;
}
