package cn.refinex.ai.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新MCP服务器请求
 *
 * @author refinex
 */
@Data
public class McpServerUpdateRequest {

    /**
     * 服务器名称
     */
    @NotBlank(message = "服务器名称不能为空")
    @Size(max = 128, message = "服务器名称长度不能超过128个字符")
    private String serverName;

    /**
     * 传输类型(stdio/sse)
     */
    @NotBlank(message = "传输类型不能为空")
    @Size(max = 16, message = "传输类型长度不能超过16个字符")
    private String transportType;

    /**
     * SSE 端点地址
     */
    @Size(max = 255, message = "端点地址长度不能超过255个字符")
    private String endpointUrl;

    /**
     * stdio 启动命令
     */
    @Size(max = 255, message = "启动命令长度不能超过255个字符")
    private String command;

    /**
     * stdio 启动参数
     */
    @Size(max = 1024, message = "启动参数长度不能超过1024个字符")
    private String args;

    /**
     * 环境变量(加密存储敏感值)
     */
    private String envVars;

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
     * 扩展信息
     */
    private String extJson;
}
