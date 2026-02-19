package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建值集请求
 *
 * @author refinex
 */
@Data
public class ValueSetCreateRequest {

    /**
     * 值集编码
     */
    @NotBlank(message = "值集编码不能为空")
    @Size(max = 64, message = "值集编码长度不能超过64个字符")
    private String setCode;

    /**
     * 值集名称
     */
    @NotBlank(message = "值集名称不能为空")
    @Size(max = 128, message = "值集名称长度不能超过128个字符")
    private String setName;

    /**
     * 状态 1启用 0停用
     */
    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status;

    /**
     * 排序
     */
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;

    /**
     * 描述
     */
    @Size(max = 255, message = "描述长度不能超过255个字符")
    private String description;
}
