package cn.refinex.ai.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档切片 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_document_chunk")
public class KbDocumentChunkDo extends BaseEntity {

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 知识库ID(冗余，避免联表)
     */
    private Long knowledgeBaseId;

    /**
     * 切片序号(从0开始)
     */
    private Integer chunkIndex;

    /**
     * 切片文本内容
     */
    private String content;

    /**
     * 切片token数
     */
    private Integer tokenCount;

    /**
     * 在原文中的起始字符偏移
     */
    private Integer startOffset;

    /**
     * 在原文中的结束字符偏移
     */
    private Integer endOffset;

    /**
     * 向量数据库中的向量ID(UUID)
     */
    private String embeddingId;

    /**
     * 切片元数据(如标题层级、页码等)
     */
    private String metadata;
}
