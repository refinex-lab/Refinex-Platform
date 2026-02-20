package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

/**
 * 查询菜单树参数
 *
 * @author refinex
 */
@Data
public class MenuTreeQuery {

    /**
     * 企业ID（平台级为0）
     */
    @PositiveOrZero(message = "企业ID不能小于0")
    private Long estabId;

    /**
     * 系统ID
     */
    @Positive(message = "系统ID必须大于0")
    private Long systemId;

    /**
     * 角色ID（可选）
     */
    @Positive(message = "角色ID必须大于0")
    private Long roleId;
}
