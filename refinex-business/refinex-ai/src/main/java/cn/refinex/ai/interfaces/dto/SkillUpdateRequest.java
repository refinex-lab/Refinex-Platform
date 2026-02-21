package cn.refinex.ai.interfaces.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 更新技能请求
 *
 * @author refinex
 */
@Data
public class SkillUpdateRequest {

    /**
     * 技能名称
     */
    @NotBlank(message = "技能名称不能为空")
    @Size(max = 128, message = "技能名称长度不能超过128个字符")
    private String skillName;

    /**
     * 技能描述
     */
    @Size(max = 1024, message = "技能描述长度不能超过1024个字符")
    private String description;

    /**
     * 图标
     */
    @Size(max = 255, message = "图标地址长度不能超过255个字符")
    private String icon;

    /**
     * 推荐使用的模型ID(可被用户覆盖)
     */
    @Positive(message = "模型ID必须大于0")
    private Long modelId;

    /**
     * 关联的Prompt模板ID
     */
    @Positive(message = "Prompt模板ID必须大于0")
    private Long promptTemplateId;

    /**
     * 温度参数(0.00-2.00)
     */
    @DecimalMin(value = "0.00", message = "温度参数不能小于0")
    @DecimalMax(value = "2.00", message = "温度参数不能大于2")
    private BigDecimal temperature;

    /**
     * Top P参数(0.00-1.00)
     */
    @DecimalMin(value = "0.00", message = "Top P参数不能小于0")
    @DecimalMax(value = "1.00", message = "Top P参数不能大于1")
    private BigDecimal topP;

    /**
     * 最大输出token数
     */
    @Min(value = 1, message = "最大输出token数不能小于1")
    private Integer maxTokens;

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
     * 扩展信息(如 Advisor 链配置)
     */
    private String extJson;

    /**
     * 关联的工具ID列表
     */
    private List<Long> toolIds;
}
