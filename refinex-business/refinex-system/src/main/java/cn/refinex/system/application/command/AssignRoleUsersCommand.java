package cn.refinex.system.application.command;

import lombok.Data;

import java.util.List;

/**
 * 角色用户授权命令
 *
 * @author refinex
 */
@Data
public class AssignRoleUsersCommand {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 用户ID列表
     */
    private List<Long> userIds;
    
    /**
     * 操作人用户ID
     */
    private Long operatorUserId;
}
