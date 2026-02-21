package cn.refinex.ai.application.command;

import lombok.Data;

/**
 * 更新工具命令
 *
 * @author refinex
 */
@Data
public class UpdateToolCommand {

    /**
     * 主键ID
     */
    private Long toolId;

    /**
     * 工具显示名称
     */
    private String toolName;

    /**
     * 工具类型(FUNCTION/MCP/HTTP)
     */
    private String toolType;

    /**
     * 工具描述(供 AI 模型理解用途)
     */
    private String description;

    /**
     * 输入参数 JSON Schema
     */
    private String inputSchema;

    /**
     * 输出结果 JSON Schema
     */
    private String outputSchema;

    /**
     * 处理器引用(FUNCTION:Spring Bean名; MCP:server_id; HTTP:endpoint URL)
     */
    private String handlerRef;

    /**
     * 是否需要用户确认后执行 1是 0否
     */
    private Integer requireConfirm;

    /**
     * 是否内置 1是 0否
     */
    private Integer isBuiltin;

    /**
     * 停用
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
     * 扩展信息(如超时配置、重试策略)
     */
    private String extJson;
}
