package cn.refinex.ai.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库定义 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_knowledge_base")
public class KbKnowledgeBaseDo extends BaseEntity {

    /**
     * 组织ID
     */
    private Long estabId;

    /**
     * 知识库编码
     */
    private String kbCode;

    /**
     * 知识库名称
     */
    private String kbName;

    /**
     * 知识库描述
     */
    private String description;

    /**
     * 图标
     */
    private String icon;

    /**
     * 可见性 0私有 1组织内公开 2平台公开
     */
    private Integer visibility;

    /**
     * 是否开启向量化 1是 0否
     */
    private Integer vectorized;

    /**
     * 嵌入模型ID(开启向量化时必填)
     */
    private Long embeddingModelId;

    /**
     * 文档切片大小(token数)
     */
    private Integer chunkSize;

    /**
     * 切片重叠大小(token数)
     */
    private Integer chunkOverlap;

    /**
     * 文档数量(冗余计数)
     */
    private Integer docCount;

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
     * 扩展信息
     */
    private String extJson;
}
