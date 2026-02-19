package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建角色请求
 *
 * @author refinex
 */
@Data
public class RoleCreateRequest {

    /**
     * 系统ID
     */
    @NotNull(message = "系统ID不能为空")
    @Positive(message = "系统ID必须大于0")
    private Long systemId;

    /**
     * 企业ID（0表示平台级角色）
     */
    @PositiveOrZero(message = "企业ID不能小于0")
    private Long estabId;

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 64, message = "角色编码长度不能超过64个字符")
    private String roleCode;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 128, message = "角色名称长度不能超过128个字符")
    private String roleName;

    /**
     * 角色类型 0系统内置 1租户内置 2自定义
     */
    @Min(value = 0, message = "角色类型取值非法")
    @Max(value = 2, message = "角色类型取值非法")
    private Integer roleType;

    /**
     * 数据范围 0全部 1本人 2团队/部门 3自定义
     */
    @Min(value = 0, message = "数据范围取值非法")
    @Max(value = 3, message = "数据范围取值非法")
    private Integer dataScopeType;

    /**
     * 父角色ID
     */
    @PositiveOrZero(message = "父角色ID不能小于0")
    private Long parentRoleId;

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
