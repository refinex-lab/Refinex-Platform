package cn.refinex.ai.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新供应商请求
 *
 * @author refinex
 */
@Data
public class ProviderUpdateRequest {

    /**
     * 供应商名称
     */
    @NotBlank(message = "供应商名称不能为空")
    @Size(max = 128, message = "供应商名称长度不能超过128个字符")
    private String providerName;

    /**
     * 接口协议(openai/anthropic/ollama)
     */
    @Size(max = 32, message = "接口协议长度不能超过32个字符")
    private String protocol;

    /**
     * 默认 API 基础地址
     */
    @Size(max = 255, message = "API基础地址长度不能超过255个字符")
    private String baseUrl;

    /**
     * 供应商图标地址
     */
    @Size(max = 255, message = "图标地址长度不能超过255个字符")
    private String iconUrl;

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
     * 扩展信息
     */
    private String extJson;
}
