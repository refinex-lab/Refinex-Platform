package cn.refinex.auth.domain.entity;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 角色-菜单授权
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scr_role_menu")
public class ScrRoleMenu extends BaseEntity {

    private Long roleId;

    private Long menuId;

    private Long grantedBy;

    private LocalDateTime grantedTime;
}
