package cn.refinex.system.domain.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 团队成员关系实体
 *
 * @author refinex
 */
@Data
public class TeamUserEntity {

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
     * 团队角色
     */
    private Integer roleInTeam;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 是否删除
     */
    private Integer deleted;
}
