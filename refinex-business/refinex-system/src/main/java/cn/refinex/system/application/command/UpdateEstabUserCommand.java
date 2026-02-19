package cn.refinex.system.application.command;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 更新企业成员关系命令
 *
 * @author refinex
 */
@Data
public class UpdateEstabUserCommand {

    /**
     * 企业成员关系ID
     */
    private Long estabUserId;

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
     * 离开时间
     */
    private LocalDateTime leaveTime;

    /**
     * 职位
     */
    private String positionTitle;
}
