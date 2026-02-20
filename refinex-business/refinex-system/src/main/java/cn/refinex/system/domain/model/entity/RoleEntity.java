package cn.refinex.system.domain.model.entity;

import lombok.Data;

/**
 * 角色实体
 *
 * @author refinex
 */
@Data
public class RoleEntity {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 企业ID（平台级为0）
     */
    private Long estabId;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色类型 0系统内置 1租户内置
     */
    private Integer roleType;

    /**
     * 是否内置 1是 0否
     */
    private Integer isBuiltin;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 排序(升序)
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否删除 1是 0否
     */
    private Integer deleted;
}
