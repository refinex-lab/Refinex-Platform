package cn.refinex.ai.domain.model.entity;

import lombok.Data;

/**
 * 文档切片领域实体
 *
 * @author refinex
 */
@Data
public class DocumentChunkEntity {

    /**
     * 主键ID
     */
    private Long id;

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

    /**
     * 逻辑删除 0未删 1已删
     */
    private Integer deleted;
}
