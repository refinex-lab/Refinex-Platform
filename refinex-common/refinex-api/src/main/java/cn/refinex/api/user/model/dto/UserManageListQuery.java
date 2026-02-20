package cn.refinex.api.user.model.dto;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 用户管理列表查询参数
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserManageListQuery extends PageRequest {

    /**
     * 主组织ID
     */
    private Long primaryEstabId;

    /**
     * 用户状态 1启用 2停用 3锁定
     */
    private Integer status;

    /**
     * 用户类型 0平台 1租户 2合作方
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
     * 关键字（用户名/显示名/昵称/手机号/邮箱）
     */
    private String keyword;

    /**
     * 用户ID列表（可选）
     */
    private List<Long> userIds;

}
