package cn.refinex.system.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 团队成员关系 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("def_team_user")
public class DefTeamUserDo extends BaseEntity {

    /**
     * 团队ID
     */
    private Long teamId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 团队角色 0成员 1负责人
     */
    private Integer roleInTeam;

    /**
     * 状态 1有效 2禁用
     */
    private Integer status;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;
}
