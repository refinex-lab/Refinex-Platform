package cn.refinex.auth.domain.entity;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 团队成员关系
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("def_team_user")
public class DefTeamUser extends BaseEntity {

    private Long teamId;

    private Long userId;

    private Integer roleInTeam;

    private Integer status;

    private LocalDateTime joinTime;
}
