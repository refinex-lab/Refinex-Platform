package cn.refinex.system.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业成员关系视图对象
 *
 * @author refinex
 */
@Data
public class EstabUserVO {

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
     * 用户名
     */
    private String username;

    /**
     * 用户编码
     */
    private String userCode;

    /**
     * 用户显示名称
     */
    private String displayName;

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
