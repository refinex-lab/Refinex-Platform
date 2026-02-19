package cn.refinex.system.interfaces.dto;

import lombok.Data;

/**
 * 系统用户列表查询参数
 *
 * @author refinex
 */
@Data
public class SystemUserListQuery {

    /**
     * 主要组织ID
     */
    private Long primaryEstabId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 关键字
     */
    private String keyword;

    /**
     * 限制条数
     */
    private Integer limit;
}
