package cn.refinex.system.application.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色授权信息 DTO
 *
 * @author refinex
 */
@Data
public class RoleBindingDTO {

    /**
     * 用户ID列表
     */
    private List<Long> userIds = new ArrayList<>();

    /**
     * 菜单ID列表
     */
    private List<Long> menuIds = new ArrayList<>();

    /**
     * 菜单操作ID列表
     */
    private List<Long> menuOpIds = new ArrayList<>();
}
