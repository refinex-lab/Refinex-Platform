package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 更新团队命令
 *
 * @author refinex
 */
@Data
public class UpdateTeamCommand {

    /**
     * 团队ID
     */
    private Long teamId;

    /**
     * 团队名称
     */
    private String teamName;

    /**
     * 父团队ID
     */
    private Long parentId;

    /**
     * 负责人用户ID
     */
    private Long leaderUserId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;
}
