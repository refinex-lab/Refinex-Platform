package cn.refinex.ai.application.dto;

import lombok.Data;

import java.util.Map;

/**
 * RAG 检索结果 DTO
 *
 * @author refinex
 */
@Data
public class SearchResultDTO {

    /**
     * 匹配的文本内容
     */
    private String content;

    /**
     * 相似度分数
     */
    private Double score;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;
}
