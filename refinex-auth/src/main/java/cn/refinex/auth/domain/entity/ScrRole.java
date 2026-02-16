package cn.refinex.auth.domain.entity;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scr_role")
public class ScrRole extends BaseEntity {

    private Long systemId;

    private Long estabId;

    private String roleCode;

    private String roleName;

    private Integer roleType;

    private Integer dataScopeType;

    private Long parentRoleId;

    private Integer isBuiltin;

    private Integer status;

    private Integer sort;

    private String remark;
}
