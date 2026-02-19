package cn.refinex.system.interfaces.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 团队成员更新请求
 *
 * @author refinex
 */
@Data
public class TeamUserUpdateRequest {

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
