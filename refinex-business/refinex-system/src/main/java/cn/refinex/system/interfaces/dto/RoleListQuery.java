package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 查询角色列表参数
 *
 * @author refinex
 */
@Data
public class RoleListQuery {

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
     * 状态 1启用 2停用
     */
    @Min(value = 1, message = "状态取值非法")
    @Max(value = 2, message = "状态取值非法")
    private Integer status;

    /**
     * 关键字（角色编码/名称）
     */
    @Size(max = 64, message = "关键字长度不能超过64个字符")
    private String keyword;
}
