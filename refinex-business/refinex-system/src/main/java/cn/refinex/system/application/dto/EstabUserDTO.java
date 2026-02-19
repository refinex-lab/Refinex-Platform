package cn.refinex.system.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业成员关系 DTO
 *
 * @author refinex
 */
@Data
public class EstabUserDTO {

    /**
     * 关系ID
     */
    private Long id;

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 成员类型
     */
    private Integer memberType;

    /**
     * 是否管理员
     */
    private Integer isAdmin;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 离开时间
     */
    private LocalDateTime leaveTime;

    /**
     * 职位
     */
    private String positionTitle;
}
