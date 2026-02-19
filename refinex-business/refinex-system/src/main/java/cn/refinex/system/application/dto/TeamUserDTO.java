package cn.refinex.system.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 团队成员关系 DTO
 *
 * @author refinex
 */
@Data
public class TeamUserDTO {

    /**
     * 关系ID
     */
    private Long id;

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
