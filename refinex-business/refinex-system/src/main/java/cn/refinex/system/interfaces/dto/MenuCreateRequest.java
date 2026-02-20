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
 * 创建菜单请求
 *
 * @author refinex
 */
@Data
public class MenuCreateRequest {

    /**
     * 系统ID
     */
    @NotNull(message = "系统ID不能为空")
    @Positive(message = "系统ID必须大于0")
    private Long systemId;

    /**
     * 父菜单ID（0表示根菜单）
     */
    @PositiveOrZero(message = "父菜单ID不能小于0")
    private Long parentId;

    /**
     * 菜单编码（可选，留空自动生成）
     */
    @Size(max = 64, message = "菜单编码长度不能超过64个字符")
    private String menuCode;

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 128, message = "菜单名称长度不能超过128个字符")
    private String menuName;

    /**
     * 菜单类型 0目录 1菜单 2按钮
     */
    @Min(value = 0, message = "菜单类型取值非法")
    @Max(value = 2, message = "菜单类型取值非法")
    private Integer menuType;

    /**
     * 路由路径
     */
    @Size(max = 255, message = "路由路径长度不能超过255个字符")
    private String path;

    /**
     * 前端组件
     */
    @Size(max = 255, message = "组件路径长度不能超过255个字符")
    private String component;

    /**
     * 权限标识
     */
    @Size(max = 128, message = "权限标识长度不能超过128个字符")
    private String permissionKey;

    /**
     * 图标
     */
    @Size(max = 64, message = "图标长度不能超过64个字符")
    private String icon;

    /**
     * 是否可见 1可见 0隐藏
     */
    @Min(value = 0, message = "可见性取值非法")
    @Max(value = 1, message = "可见性取值非法")
    private Integer visible;

    /**
     * 是否外链 1是 0否
     */
    @Min(value = 0, message = "外链标识取值非法")
    @Max(value = 1, message = "外链标识取值非法")
    private Integer isFrame;

    /**
     * 是否缓存 1是 0否
     */
    @Min(value = 0, message = "缓存标识取值非法")
    @Max(value = 1, message = "缓存标识取值非法")
    private Integer isCache;

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
}
