package cn.refinex.api.user.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户管理-批量删除命令
 *
 * @author refinex
 */
@Data
public class UserManageBatchDeleteCommand implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID列表
     */
    private List<Long> userIds;
}
