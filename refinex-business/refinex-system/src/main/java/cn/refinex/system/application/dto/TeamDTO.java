package cn.refinex.system.application.dto;

import lombok.Data;

/**
 * 团队 DTO
 *
 * @author refinex
 */
@Data
public class TeamDTO {

    /**
     * 团队ID
     */
    private Long id;

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 团队编码
     */
    private String teamCode;

    /**
     * 团队名称
     */
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
