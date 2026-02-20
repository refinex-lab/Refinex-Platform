package cn.refinex.system.application.command;

import lombok.Data;

import java.util.List;

/**
 * 角色菜单与操作授权命令
 *
 * @author refinex
 */
@Data
public class AssignRolePermissionsCommand {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID列表
     */
    private List<Long> menuIds;

    /**
     * 菜单操作ID列表
     */
    private List<Long> menuOpIds;

    /**
     * 数据资源接口ID列表
     */
    private List<Long> drsInterfaceIds;

    /**
     * 操作人用户ID
     */
    private Long operatorUserId;
}
