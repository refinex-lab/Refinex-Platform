package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 查询菜单树参数
 *
 * @author refinex
 */
@Data
public class MenuTreeQuery {

    /**
     * 系统ID
     */
    @NotNull(message = "系统ID不能为空")
    @Positive(message = "系统ID必须大于0")
    private Long systemId;

    /**
     * 角色ID（可选，传入后返回授权标记）
     */
    @Positive(message = "角色ID必须大于0")
    private Long roleId;
}
