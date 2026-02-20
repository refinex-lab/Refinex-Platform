package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新角色请求
 *
 * @author refinex
 */
@Data
public class RoleUpdateRequest {

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 128, message = "角色名称长度不能超过128个字符")
    private String roleName;

    /**
     * 角色类型 0系统内置 1租户内置
     */
    @Min(value = 0, message = "角色类型取值非法")
    @Max(value = 1, message = "角色类型取值非法")
    private Integer roleType;

    /**
     * 是否内置 1是 0否
     */
    @Min(value = 0, message = "内置标识取值非法")
    @Max(value = 1, message = "内置标识取值非法")
    private Integer isBuiltin;

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
