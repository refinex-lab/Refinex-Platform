package cn.refinex.auth.domain.entity;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 组织成员关系
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("def_estab_user")
public class DefEstabUser extends BaseEntity {

    private Long estabId;

    private Long userId;

    private Integer memberType;

    private Integer isAdmin;

    private Integer status;

    private LocalDateTime joinTime;

    private LocalDateTime leaveTime;

    private String positionTitle;
}
