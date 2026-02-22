package cn.refinex.ai.application.command;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新模型命令
 *
 * @author refinex
 */
@Data
public class UpdateModelCommand {

    /**
     * 主键ID
     */
    private Long modelId;

    /**
     * 模型显示名称
     */
    private String modelName;

    /**
     * 模型类型 1聊天 2嵌入 3图像生成 4语音转文字 5文字转语音 6重排序
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
