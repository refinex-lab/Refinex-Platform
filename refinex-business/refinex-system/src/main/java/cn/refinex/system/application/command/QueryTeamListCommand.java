package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 团队列表查询命令
 *
 * @author refinex
 */
@Data
public class QueryTeamListCommand {

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 父团队ID
     */
    private Long parentId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 关键字
     */
    private String keyword;
}
