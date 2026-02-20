package cn.refinex.system.interfaces.dto;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户列表查询参数
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SystemUserListQuery extends PageRequest {

    /**
     * 主要组织ID
     */
    private Long primaryEstabId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 用户编码（模糊匹配）
     */
    private String userCode;

    /**
     * 用户名（模糊匹配）
     */
    private String username;

    /**
     * 显示名称（模糊匹配）
     */
    private String displayName;

    /**
     * 昵称（模糊匹配）
     */
    private String nickname;

    /**
     * 主手机号（模糊匹配）
     */
    private String primaryPhone;

    /**
     * 主邮箱（模糊匹配）
     */
    private String primaryEmail;

    /**
     * 关键字（兼容字段）
     */
    private String keyword;

}
