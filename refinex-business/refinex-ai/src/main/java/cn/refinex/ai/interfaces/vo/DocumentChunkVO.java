package cn.refinex.ai.interfaces.vo;

import lombok.Data;

/**
 * 文档切片 VO
 *
 * @author refinex
 */
@Data
public class DocumentChunkVO {

    /**
     * 主键ID
     */
    private Long id;

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
}
