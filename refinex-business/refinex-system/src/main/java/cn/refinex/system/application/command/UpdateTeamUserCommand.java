package cn.refinex.system.application.command;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 更新团队成员关系命令
 *
 * @author refinex
 */
@Data
public class UpdateTeamUserCommand {

    /**
     * 团队成员关系ID
     */
    private Long teamUserId;

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
