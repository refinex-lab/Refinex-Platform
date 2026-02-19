package cn.refinex.system.application.dto;

import lombok.Data;

/**
 * 团队成员候选用户 DTO
 *
 * @author refinex
 */
@Data
public class TeamUserCandidateDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户编码
     */
    private String userCode;

    /**
     * 显示名称
     */
    private String displayName;
}
