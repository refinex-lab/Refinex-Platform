package cn.refinex.ai.interfaces.dto;

import cn.refinex.base.request.PageRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模型列表查询
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModelListQuery extends PageRequest {

    /**
     * 供应商ID
     */
    @Positive(message = "供应商ID必须大于0")
    private Long providerId;

    /**
     * 模型类型 1聊天 2嵌入 3图像生成 4语音转文字 5文字转语音 6重排序 7内容审核
     */
    @Min(value = 1, message = "模型类型取值非法")
    @Max(value = 6, message = "模型类型取值非法")
    private Integer modelType;

    /**
     * 状态 1启用 0停用
     */
    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status;

    /**
     * 关键字
     */
    @Size(max = 64, message = "关键字长度不能超过64个字符")
    private String keyword;
}
