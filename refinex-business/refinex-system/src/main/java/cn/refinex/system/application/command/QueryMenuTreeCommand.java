package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 查询菜单树命令
 *
 * @author refinex
 */
@Data
public class QueryMenuTreeCommand {

    /**
     * 系统ID
     */
    private Long systemId;

    /**
     * 角色ID
     */
    private Long roleId;
}
