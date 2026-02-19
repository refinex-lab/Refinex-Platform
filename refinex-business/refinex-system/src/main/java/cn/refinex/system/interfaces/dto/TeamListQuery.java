package cn.refinex.system.interfaces.dto;

import cn.refinex.base.request.PageRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 团队列表查询参数
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamListQuery extends PageRequest {

    /**
     * 企业ID
     */
    @NotNull(message = "企业ID不能为空")
    @Positive(message = "企业ID必须大于0")
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
