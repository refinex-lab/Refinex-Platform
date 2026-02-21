package cn.refinex.ai.application.dto;

import lombok.Data;

/**
 * MCP服务器 DTO
 *
 * @author refinex
 */
@Data
public class McpServerDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 组织ID(平台级为0)
     */
    private Long estabId;

    /**
     * 服务器编码
     */
    private String serverCode;

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
