package cn.refinex.ai.domain.model.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 技能-知识库关联领域实体
 *
 * @author refinex
 */
@Data
public class SkillKnowledgeEntity {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 技能ID
     */
    private Long skillId;

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * RAG检索返回文档数
     */
    private Integer topK;

    /**
     * 相似度阈值(0.000-1.000)
     */
    private BigDecimal similarityThreshold;

    /**
     * 排序(升序)
     */
    private Integer sort;

    /**
     * 逻辑删除 0未删 1已删
     */
    private Integer deleted;
}
