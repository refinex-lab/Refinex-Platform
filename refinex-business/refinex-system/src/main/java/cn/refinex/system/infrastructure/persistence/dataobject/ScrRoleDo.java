package cn.refinex.system.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scr_role")
public class ScrRoleDo extends BaseEntity {

    /**
     * 系统ID
     */
    private Long systemId;

    /**
     * 企业ID
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
     * 是否内置 0否 1是
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
}
