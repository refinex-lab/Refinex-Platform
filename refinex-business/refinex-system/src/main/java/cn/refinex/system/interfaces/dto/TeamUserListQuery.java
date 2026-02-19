package cn.refinex.system.interfaces.dto;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 团队成员列表查询参数
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamUserListQuery extends PageRequest {

    /**
     * 状态
     */
    private Integer status;
}
