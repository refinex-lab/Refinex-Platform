package cn.refinex.ai.interfaces.dto;

import cn.refinex.base.request.PageRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户模型开通列表查询
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModelProvisionListQuery extends PageRequest {

    /**
     * 组织ID
     */
    @Positive(message = "组织ID必须大于0")
    private Long estabId;

    /**
     * 模型ID
     */
    @Positive(message = "模型ID必须大于0")
    private Long modelId;

    /**
     * 状态 1启用 0停用
     */
    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status;
}
