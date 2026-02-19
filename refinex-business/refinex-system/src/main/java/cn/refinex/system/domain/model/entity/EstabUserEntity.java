package cn.refinex.system.domain.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业成员关系实体
 *
 * @author refinex
 */
@Data
public class EstabUserEntity {

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

    /**
     * 是否删除
     */
    private Integer deleted;
}
