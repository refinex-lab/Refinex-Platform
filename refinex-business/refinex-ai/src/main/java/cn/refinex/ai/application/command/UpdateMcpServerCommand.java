package cn.refinex.ai.application.command;

import lombok.Data;

/**
 * 更新MCP服务器命令
 *
 * @author refinex
 */
@Data
public class UpdateMcpServerCommand {

    /**
     * 主键ID
     */
    private Long mcpServerId;

    /**
     * 服务器名称
     */
    private String serverName;

    /**
     * 传输类型(stdio/sse)
     */
    private String transportType;

    /**
     * SSE 端点地址
     */
    private String endpointUrl;

    /**
     * stdio 启动命令
     */
    private String command;

    /**
     * stdio 启动参数
     */
    private String args;

    /**
     * 环境变量(加密存储敏感值)
     */
    private String envVars;

    /**
     * 状态 1启用 0停用
     */
    private Integer status;

    /**
     * 排序(升序)
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展信息
     */
    private String extJson;
}
