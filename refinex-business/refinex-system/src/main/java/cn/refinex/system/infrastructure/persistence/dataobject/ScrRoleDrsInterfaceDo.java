package cn.refinex.system.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色数据资源接口授权 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scr_role_drs_interface")
public class ScrRoleDrsInterfaceDo extends BaseEntity {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 数据资源接口ID
     */
    private Long drsInterfaceId;
}
