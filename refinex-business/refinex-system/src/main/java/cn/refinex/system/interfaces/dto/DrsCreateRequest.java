package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建数据资源请求
 *
 * @author refinex
 */
@Data
public class DrsCreateRequest {

    /**
     * 数据资源编码（可选，留空自动生成）
     */
    @Size(max = 64, message = "数据资源编码长度不能超过64个字符")
    private String drsCode;

    /**
     * 数据资源名称
     */
    @NotBlank(message = "数据资源名称不能为空")
    @Size(max = 128, message = "数据资源名称长度不能超过128个字符")
    private String drsName;

    /**
     * 所属组织ID(平台级为0)
     */
    @PositiveOrZero(message = "所属组织ID不能小于0")
    private Long ownerEstabId;

    /**
     * 数据归属 0平台 1租户
     */
    @Min(value = 0, message = "数据归属取值非法")
    @Max(value = 1, message = "数据归属取值非法")
    private Integer dataOwnerType;

    /**
     * 状态 1启用 2停用
     */
    @Min(value = 1, message = "状态取值非法")
    @Max(value = 2, message = "状态取值非法")
    private Integer status;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
