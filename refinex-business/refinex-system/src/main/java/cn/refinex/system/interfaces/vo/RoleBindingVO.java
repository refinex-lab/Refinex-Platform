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
     * 已绑定用户列表
     */
    private List<RoleBindingUserVO> users = new ArrayList<>();

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

    /**
     * 数据资源接口ID列表
     */
    private List<Long> drsInterfaceIds = new ArrayList<>();
}
