package cn.refinex.ai.application.command;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询租户模型开通列表命令
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryModelProvisionListCommand extends PageRequest {

    /**
     * 组织ID
     */
    private Long estabId;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 状态 1启用 0停用
     */
    private Integer status;
}
