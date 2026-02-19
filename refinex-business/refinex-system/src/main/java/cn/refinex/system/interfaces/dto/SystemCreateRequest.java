package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建系统请求
 *
 * @author refinex
 */
@Data
public class SystemCreateRequest {

    /**
     * 系统编码
     */
    @NotBlank(message = "系统编码不能为空")
    @Size(max = 64, message = "系统编码长度不能超过64个字符")
    private String systemCode;

    /**
     * 系统名称
     */
    @NotBlank(message = "系统名称不能为空")
    @Size(max = 128, message = "系统名称长度不能超过128个字符")
    private String systemName;

    /**
     * 系统类型 0平台 1租户 2业务子系统
     */
    @Min(value = 0, message = "系统类型取值非法")
    @Max(value = 2, message = "系统类型取值非法")
    private Integer systemType;

    /**
     * 系统基础URL
     */
    @Size(max = 255, message = "系统地址长度不能超过255个字符")
    private String baseUrl;

    /**
     * 状态 1启用 2停用
     */
    @Min(value = 1, message = "状态取值非法")
    @Max(value = 2, message = "状态取值非法")
    private Integer status;

    /**
     * 排序
     */
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
