package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建值集明细请求
 *
 * @author refinex
 */
@Data
public class ValueCreateRequest {

    /**
     * 值编码
     */
    @NotBlank(message = "值编码不能为空")
    @Size(max = 64, message = "值编码长度不能超过64个字符")
    private String valueCode;

    /**
     * 值名称
     */
    @NotBlank(message = "值名称不能为空")
    @Size(max = 128, message = "值名称长度不能超过128个字符")
    private String valueName;

    /**
     * 值描述
     */
    @Size(max = 255, message = "值描述长度不能超过255个字符")
    private String valueDesc;

    /**
     * 状态 1启用 0停用
     */
    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status;

    /**
     * 是否默认 1是 0否
     */
    @Min(value = 0, message = "默认标识取值非法")
    @Max(value = 1, message = "默认标识取值非法")
    private Integer isDefault;

    /**
     * 排序
     */
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;
}
