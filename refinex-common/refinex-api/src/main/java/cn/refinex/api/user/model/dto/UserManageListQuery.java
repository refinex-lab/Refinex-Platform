package cn.refinex.api.user.model.dto;

import cn.refinex.base.request.PageRequest;
import lombok.Data;

/**
 * 用户管理列表查询参数
 *
 * @author refinex
 */
@Data
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
     * 关键字（用户名/显示名/昵称/手机号/邮箱）
     */
    private String keyword;

}
