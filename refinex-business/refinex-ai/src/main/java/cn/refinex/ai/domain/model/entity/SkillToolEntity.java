package cn.refinex.ai.domain.model.entity;

import lombok.Data;

/**
 * 技能-工具关联领域实体
 *
 * @author refinex
 */
@Data
public class SkillToolEntity {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 技能ID
     */
    private Long skillId;

    /**
     * 工具ID
     */
    private Long toolId;

    /**
     * 排序(升序)
     */
    private Integer sort;

    /**
     * 逻辑删除 0未删 1已删
     */
    private Integer deleted;
}
