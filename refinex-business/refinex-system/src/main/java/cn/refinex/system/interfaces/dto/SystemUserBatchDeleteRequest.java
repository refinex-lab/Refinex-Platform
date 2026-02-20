package cn.refinex.system.interfaces.dto;

import lombok.Data;

import java.util.List;

/**
 * 系统用户批量删除请求
 *
 * @author refinex
 */
@Data
public class SystemUserBatchDeleteRequest {

    /**
     * 用户ID列表
     */
    private List<Long> userIds;
}
