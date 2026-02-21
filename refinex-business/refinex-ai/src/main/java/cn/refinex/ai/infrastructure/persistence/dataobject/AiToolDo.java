package cn.refinex.ai.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI工具定义 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_tool")
public class AiToolDo extends BaseEntity {

    /**
     * 组织ID(平台级为0)
     */
    private Long estabId;

    /**
     * 工具编码(全局唯一标识)
     */
    private String toolCode;

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
     * 扩展信息(如超时配置、重试策略)
     */
    private String extJson;
}
