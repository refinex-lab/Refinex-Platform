package cn.refinex.ai.interfaces.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新模型请求
 *
 * @author refinex
 */
@Data
public class ModelUpdateRequest {

    /**
     * 模型显示名称
     */
    @NotBlank(message = "模型名称不能为空")
    @Size(max = 128, message = "模型名称长度不能超过128个字符")
    private String modelName;

    /**
     * 模型类型 1聊天 2嵌入 3图像生成 4语音转文字 5文字转语音 6重排序 7内容审核
     */
    @Min(value = 1, message = "模型类型取值非法")
    @Max(value = 6, message = "模型类型取值非法")
    private Integer modelType;

    /**
     * 能力:视觉理解 1支持 0不支持
     */
    @Min(value = 0, message = "能力标识取值非法")
    @Max(value = 1, message = "能力标识取值非法")
    private Integer capVision;

    /**
     * 能力:工具调用 1支持 0不支持
     */
    @Min(value = 0, message = "能力标识取值非法")
    @Max(value = 1, message = "能力标识取值非法")
    private Integer capToolCall;

    /**
     * 能力:结构化输出 1支持 0不支持
     */
    @Min(value = 0, message = "能力标识取值非法")
    @Max(value = 1, message = "能力标识取值非法")
    private Integer capStructuredOutput;

    /**
     * 能力:流式输出 1支持 0不支持
     */
    @Min(value = 0, message = "能力标识取值非法")
    @Max(value = 1, message = "能力标识取值非法")
    private Integer capStreaming;

    /**
     * 能力:深度推理 1支持 0不支持
     */
    @Min(value = 0, message = "能力标识取值非法")
    @Max(value = 1, message = "能力标识取值非法")
    private Integer capReasoning;

    /**
     * 最大上下文窗口(token数)
     */
    @Positive(message = "最大上下文窗口必须大于0")
    private Integer maxContextWindow;

    /**
     * 最大输出token数
     */
    @Positive(message = "最大输出token数必须大于0")
    private Integer maxOutputTokens;

    /**
     * 输入价格(每百万token/美元)
     */
    @DecimalMin(value = "0", message = "输入价格不能小于0")
    private BigDecimal inputPrice;

    /**
     * 输出价格(每百万token/美元)
     */
    @DecimalMin(value = "0", message = "输出价格不能小于0")
    private BigDecimal outputPrice;

    /**
     * 状态 1正常 0停用
     */
    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status;

    /**
     * 排序(升序)
     */
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;

    /**
     * 扩展信息(如特殊参数默认值)
     */
    private String extJson;
}
