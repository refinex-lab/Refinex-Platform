package cn.refinex.ai.interfaces.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 模型 VO
 *
 * @author refinex
 */
@Data
public class ModelVO {

    /**
     * 模型ID
     */
    private Long id;

    /**
     * 服务提供商ID
     */
    private Long providerId;

    /**
     * 模型编码，用于API调用
     */
    private String modelCode;

    /**
     * 模型名称，用于显示
     */
    private String modelName;

    /**
     * 模型类型 1聊天 2嵌入 3图像生成 4语音转文字 5文字转语音 6重排序
     */
    private Integer modelType;

    /**
     * 视觉能力：0-不支持，1-支持
     */
    private Integer capVision;

    /**
     * 工具调用能力：0-不支持，1-支持
     */
    private Integer capToolCall;

    /**
     * 结构化输出能力：0-不支持，1-支持
     */
    private Integer capStructuredOutput;

    /**
     * 流式输出能力：0-不支持，1-支持
     */
    private Integer capStreaming;

    /**
     * 能力:深度推理 1支持 0不支持
     */
    private Integer capReasoning;

    /**
     * 最大上下文窗口大小
     */
    private Integer maxContextWindow;

    /**
     * 最大输出Token数
     */
    private Integer maxOutputTokens;

    /**
     * 输入价格（每千Token）
     */
    private BigDecimal inputPrice;

    /**
     * 输出价格（每千Token）
     */
    private BigDecimal outputPrice;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 排序值，数值越小排序越靠前
     */
    private Integer sort;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 扩展信息，JSON格式存储
     */
    private String extJson;
}
