package cn.refinex.system.interfaces.vo;

import lombok.Data;

/**
 * 角色绑定用户 VO
 *
 * @author refinex
 */
@Data
public class RoleBindingUserVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户编码
     */
    private String userCode;

    /**
     * 用户名
     */
    private String username;

    /**
     * 显示名
     */
    private String displayName;
}
