package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 更新角色命令
 *
 * @author refinex
 */
@Data
public class UpdateRoleCommand {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色类型 0系统内置 1租户内置 2自定义
     */
    private Integer roleType;

    /**
     * 数据范围 0全部 1本人 2团队/部门 3自定义
     */
    private Integer dataScopeType;

    /**
     * 父角色ID
     */
    private Long parentRoleId;

    /**
     * 是否内置 1是 0否
     */
    private Integer isBuiltin;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;
}
