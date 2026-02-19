package cn.refinex.system.interfaces.dto;

import lombok.Data;

/**
 * 团队列表查询参数
 *
 * @author refinex
 */
@Data
public class TeamListQuery {

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
