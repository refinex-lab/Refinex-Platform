package cn.refinex.system.interfaces.dto;

import cn.refinex.base.request.BaseRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 团队成员候选查询参数
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamUserCandidateQuery extends BaseRequest {

    /**
     * 用户名关键字
     */
    @NotBlank(message = "关键字不能为空")
    private String keyword;

    /**
     * 返回数量限制
     */
    @Min(value = 1, message = "limit 必须大于0")
    @Max(value = 50, message = "limit 不能超过50")
    private Integer limit = 10;
}
