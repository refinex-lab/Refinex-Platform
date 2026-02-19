package cn.refinex.system.application.command;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 团队列表查询命令
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryTeamListCommand extends PageRequest {

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
