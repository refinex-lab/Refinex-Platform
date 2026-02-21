package cn.refinex.ai.interfaces.dto;

import cn.refinex.base.request.PageRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MCP服务器列表查询
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class McpServerListQuery extends PageRequest {

    /**
     * 传输类型(stdio/sse)
     */
    @Size(max = 16, message = "传输类型长度不能超过16个字符")
    private String transportType;

    /**
     * 状态 1启用 0停用
     */
    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status;

    /**
     * 关键字
     */
    @Size(max = 64, message = "关键字长度不能超过64个字符")
    private String keyword;
}
