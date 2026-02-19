package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色权限授权请求
 *
 * @author refinex
 */
@Data
public class AssignRolePermissionsRequest {

    /**
     * 菜单ID列表
     */
    private List<@Positive(message = "菜单ID必须大于0") Long> menuIds = new ArrayList<>();

    /**
     * 菜单操作ID列表
     */
    private List<@Positive(message = "菜单操作ID必须大于0") Long> menuOpIds = new ArrayList<>();
}
