package cn.refinex.ai.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新租户模型开通请求
 *
 * @author refinex
 */
@Data
public class ModelProvisionUpdateRequest {

    /**
     * API Key明文（null表示不修改）
     */
    @Size(max = 512, message = "API Key长度不能超过512个字符")
    private String apiKey;

    /**
     * 自定义API地址(覆盖供应商默认)
     */
    @Size(max = 255, message = "API地址长度不能超过255个字符")
    private String apiBaseUrl;

    /**
     * 日调用额度(NULL不限)
     */
    @Min(value = 0, message = "日调用额度不能小于0")
    private Integer dailyQuota;

    /**
     * 月调用额度(NULL不限)
     */
    @Min(value = 0, message = "月调用额度不能小于0")
    private Integer monthlyQuota;

    /**
     * 是否该租户默认模型 1是 0否
     */
    @Min(value = 0, message = "是否默认取值非法")
    @Max(value = 1, message = "是否默认取值非法")
    private Integer isDefault;

    /**
     * 状态 1启用 0停用
     */
    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;

    /**
     * 扩展信息(如自定义请求头)
     */
    private String extJson;
}
