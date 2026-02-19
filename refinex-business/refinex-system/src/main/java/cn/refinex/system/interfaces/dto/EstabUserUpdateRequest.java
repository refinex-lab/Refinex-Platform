package cn.refinex.system.interfaces.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业成员更新请求
 *
 * @author refinex
 */
@Data
public class EstabUserUpdateRequest {

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
