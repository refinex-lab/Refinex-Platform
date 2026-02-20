package cn.refinex.system.application.dto;

import lombok.Data;

/**
 * 角色绑定用户 DTO
 *
 * @author refinex
 */
@Data
public class RoleBindingUserDTO {

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
