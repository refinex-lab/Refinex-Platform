package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 查询角色列表命令
 *
 * @author refinex
 */
@Data
public class QueryRoleListCommand {

    /**
     * 系统ID
     */
    private Long systemId;

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;
    
    /**
     * 关键字（角色编码/名称）
     */
    private String keyword;
}
