package cn.refinex.ai.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * AI模型定义 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_model")
public class AiModelDo extends BaseEntity {

    /**
     * 供应商ID
     */
    private Long providerId;

    /**
     * 模型编码(API 调用时的 model 参数值，如 gpt-4o)
     */
    private String modelCode;

    /**
     * 模型显示名称
     */
    private String modelName;

    /**
     * 模型类型 1聊天 2嵌入 3图像生成 4语音转文字 5文字转语音 6重排序 7内容审核
     */
    private Integer modelType;

    /**
     * 能力:视觉理解 1支持 0不支持
     */
    private Integer capVision;

    /**
     * 能力:工具调用 1支持 0不支持
     */
    private Integer capToolCall;

    /**
     * 能力:结构化输出 1支持 0不支持
     */
    private Integer capStructuredOutput;

    /**
     * 能力:流式输出 1支持 0不支持
     */
    private Integer capStreaming;

    /**
     * 能力:深度推理 1支持 0不支持
     */
    private Integer capReasoning;

    /**
     * 最大上下文窗口(token数)
     */
    private Integer maxContextWindow;

    /**
     * 最大输出token数
     */
    private Integer maxOutputTokens;

    /**
     * 输入价格(每百万token/美元)
     */
    private BigDecimal inputPrice;

    /**
     * 输出价格(每百万token/美元)
     */
    private BigDecimal outputPrice;

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
     * 扩展信息(如特殊参数默认值)
     */
    private String extJson;
}
