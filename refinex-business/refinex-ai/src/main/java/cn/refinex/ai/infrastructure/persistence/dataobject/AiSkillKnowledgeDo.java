package cn.refinex.ai.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 技能-知识库关联 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_skill_knowledge")
public class AiSkillKnowledgeDo extends BaseEntity {

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
}
