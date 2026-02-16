package cn.refinex.auth.domain.entity;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色-数据资源接口授权
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scr_role_drs_interface")
public class ScrRoleDrsInterface extends BaseEntity {

    private Long roleId;

    private Long drsInterfaceId;
}
