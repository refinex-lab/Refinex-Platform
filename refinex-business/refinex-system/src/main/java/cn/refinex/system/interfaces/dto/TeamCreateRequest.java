package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 团队创建请求
 *
 * @author refinex
 */
@Data
public class TeamCreateRequest {

    /**
     * 企业ID
     */
    @NotNull(message = "企业ID不能为空")
    private Long estabId;

    /**
     * 团队编码
     */
    @NotBlank(message = "团队编码不能为空")
    private String teamCode;

    /**
     * 团队名称
     */
    @NotBlank(message = "团队名称不能为空")
    private String teamName;

    /**
     * 父团队ID
     */
    private Long parentId;

    /**
     * 负责人用户ID
     */
    private Long leaderUserId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;
}
