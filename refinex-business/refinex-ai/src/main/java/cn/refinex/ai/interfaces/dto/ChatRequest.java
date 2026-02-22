package cn.refinex.ai.interfaces.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 对话请求
 *
 * @author refinex
 */
@Data
public class ChatRequest {

    /**
     * 会话唯一标识(为空则新建对话)
     */
    private String conversationId;

    /**
     * 用户消息
     */
    @Size(max = 10000, message = "消息长度不能超过10000字符")
    private String message;

    /**
     * 模型ID(可选，为空用对话已有模型或租户默认)
     */
    private Long modelId;

    /**
     * Prompt模板ID(可选，仅新建对话时生效)
     */
    private Long promptTemplateId;

    /**
     * 模板变量
     */
    private Map<String, String> templateVariables;

    /**
     * 系统提示词(直传，优先级低于 promptTemplateId)
     */
    private String systemPrompt;

    /**
     * 图像URL列表(已上传的图像CDN地址，用于多模态视觉理解)
     */
    @Size(max = 5, message = "最多支持5张图片")
    private List<String> imageUrls;

    /**
     * 音频URL(已上传的音频CDN地址，用于语音转文字后进入对话)
     */
    private String audioUrl;

    /**
     * 知识库ID列表(选择向量化知识库进行RAG检索增强)
     */
    @Size(max = 5, message = "最多支持5个知识库")
    private List<Long> knowledgeBaseIds;

    /**
     * RAG检索返回文档数(可选，默认5)
     */
    @Min(value = 1, message = "topK最小为1")
    @Max(value = 20, message = "topK最大为20")
    private Integer ragTopK;

    /**
     * RAG相似度阈值(可选，默认0.0)
     */
    @DecimalMin(value = "0.0", message = "相似度阈值最小为0.0")
    @DecimalMax(value = "1.0", message = "相似度阈值最大为1.0")
    private Double ragSimilarityThreshold;
}
