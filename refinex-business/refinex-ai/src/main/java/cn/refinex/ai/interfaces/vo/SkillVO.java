package cn.refinex.ai.interfaces.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI技能 VO
 *
 * @author refinex
 */
@Data
public class SkillVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 组织ID(平台级为0)
     */
    private Long estabId;

    /**
     * 技能编码
     */
    private String skillCode;

    /**
     * 技能名称
     */
    private String skillName;

    /**
     * 技能描述
     */
    private String description;

    /**
     * 图标
     */
    private String icon;

    /**
     * 推荐使用的模型ID(可被用户覆盖)
     */
    private Long modelId;

    /**
     * 关联的Prompt模板ID
     */
    private Long promptTemplateId;

    /**
     * 温度参数(0.00-2.00)
     */
    private BigDecimal temperature;

    /**
     * Top P参数(0.00-1.00)
     */
    private BigDecimal topP;

    /**
     * 最大输出token数
     */
    private Integer maxTokens;

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
     * 扩展信息(如 Advisor 链配置)
     */
    private String extJson;

    /**
     * 关联的工具ID列表
     */
    private List<Long> toolIds;

    /**
     * 关联的知识库ID列表
     */
    private List<Long> knowledgeBaseIds;
}
