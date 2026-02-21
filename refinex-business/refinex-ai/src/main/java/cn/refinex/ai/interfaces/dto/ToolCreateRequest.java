package cn.refinex.ai.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建工具请求
 *
 * @author refinex
 */
@Data
public class ToolCreateRequest {

    /**
     * 工具编码(全局唯一标识)
     */
    @NotBlank(message = "工具编码不能为空")
    @Size(max = 64, message = "工具编码长度不能超过64个字符")
    private String toolCode;

    /**
     * 工具显示名称
     */
    @NotBlank(message = "工具名称不能为空")
    @Size(max = 128, message = "工具名称长度不能超过128个字符")
    private String toolName;

    /**
     * 工具类型(FUNCTION/MCP/HTTP)
     */
    @NotBlank(message = "工具类型不能为空")
    @Size(max = 16, message = "工具类型长度不能超过16个字符")
    private String toolType;

    /**
     * 工具描述(供 AI 模型理解用途)
     */
    @Size(max = 1024, message = "工具描述长度不能超过1024个字符")
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
    @Size(max = 255, message = "处理器引用长度不能超过255个字符")
    private String handlerRef;

    /**
     * 是否需要用户确认后执行 1是 0否
     */
    @Min(value = 0, message = "是否需要确认取值非法")
    @Max(value = 1, message = "是否需要确认取值非法")
    private Integer requireConfirm;

    /**
     * 是否内置 1是 0否
     */
    @Min(value = 0, message = "是否内置取值非法")
    @Max(value = 1, message = "是否内置取值非法")
    private Integer isBuiltin;

    /**
     * 状态 1启用 0停用
     */
    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status;

    /**
     * 排序(升序)
     */
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;

    /**
     * 扩展信息(如超时配置、重试策略)
     */
    private String extJson;
}
