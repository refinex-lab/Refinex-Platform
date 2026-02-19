package cn.refinex.system.interfaces.dto;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户列表查询参数
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SystemUserListQuery extends PageRequest {

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

}
