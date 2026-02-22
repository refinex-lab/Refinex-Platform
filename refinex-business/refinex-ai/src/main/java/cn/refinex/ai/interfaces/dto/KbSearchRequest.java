package cn.refinex.ai.interfaces.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 知识库检索请求
 *
 * @author refinex
 */
@Data
public class KbSearchRequest {

    /**
     * 查询文本
     */
    @NotBlank(message = "查询文本不能为空")
    private String query;

    /**
     * 返回结果数量
     */
    @Min(value = 1, message = "topK最小为1")
    @Max(value = 20, message = "topK最大为20")
    private Integer topK;

    /**
     * 相似度阈值
     */
    @DecimalMin(value = "0.0", message = "相似度阈值最小为0.0")
    @DecimalMax(value = "1.0", message = "相似度阈值最大为1.0")
    private Double similarityThreshold;
}
