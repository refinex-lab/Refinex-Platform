package cn.refinex.system.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 企业成员关系 DO
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
     * 成员类型 0员工 1外包 2外部协作
     */
    private Integer memberType;

    /**
     * 是否管理员 1是 0否
     */
    private Integer isAdmin;

    /**
     * 状态 1有效 2禁用 3离职
     */
    private Integer status;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 离开时间
     */
    private LocalDateTime leaveTime;

    /**
     * 职位
     */
    private String positionTitle;
}
