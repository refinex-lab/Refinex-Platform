package cn.refinex.user.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 组织成员关系 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("def_estab_user")
public class DefEstabUserDo extends BaseEntity {

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 成员类型
     */
    private Integer memberType;

    /**
     * 是否管理员
     */
    private Integer isAdmin;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 离职时间
     */
    private LocalDateTime leaveTime;

    /**
     * 职位标题
     */
    private String positionTitle;
}
