package cn.refinex.auth.domain.entity;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 角色-菜单操作授权
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scr_role_menu_op")
public class ScrRoleMenuOp extends BaseEntity {

    private Long roleId;

    private Long menuOpId;

    private Long grantedBy;

    private LocalDateTime grantedTime;
}
