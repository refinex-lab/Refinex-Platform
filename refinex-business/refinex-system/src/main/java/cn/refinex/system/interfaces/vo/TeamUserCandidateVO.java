package cn.refinex.system.interfaces.vo;

import lombok.Data;

/**
 * 团队成员候选用户 VO
 *
 * @author refinex
 */
@Data
public class TeamUserCandidateVO {

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
