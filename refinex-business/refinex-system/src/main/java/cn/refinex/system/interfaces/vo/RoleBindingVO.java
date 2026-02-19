package cn.refinex.system.interfaces.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色授权信息 VO
 *
 * @author refinex
 */
@Data
public class RoleBindingVO {

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
