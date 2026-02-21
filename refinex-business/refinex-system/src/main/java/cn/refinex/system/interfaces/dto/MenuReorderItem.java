package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 菜单排序项
 *
 * @author refinex
 */
@Data
public class MenuReorderItem {

    /**
     * 菜单ID
     */
    @NotNull(message = "菜单ID不能为空")
    @Positive(message = "菜单ID必须大于0")
    private Long menuId;

    /**
     * 父菜单ID（0表示根菜单）
     */
    @NotNull(message = "父菜单ID不能为空")
    @Min(value = 0, message = "父菜单ID不能为负数")
    private Long parentId;

    /**
     * 排序值
     */
    @NotNull(message = "排序值不能为空")
    @Min(value = 0, message = "排序值不能为负数")
    private Integer sort;
}
