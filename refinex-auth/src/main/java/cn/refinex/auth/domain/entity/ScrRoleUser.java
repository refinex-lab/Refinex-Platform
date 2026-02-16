package cn.refinex.auth.domain.entity;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 角色-用户关系
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("scr_role_user")
public class ScrRoleUser extends BaseEntity {

    private Long roleId;

    private Long userId;

    private Long estabId;

    private Long grantedBy;

    private LocalDateTime grantedTime;

    private Integer status;
}
