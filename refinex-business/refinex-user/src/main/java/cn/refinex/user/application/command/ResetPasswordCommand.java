package cn.refinex.user.application.command;

import lombok.Data;

/**
 * 重置密码命令
 *
 * @author refinex
 */
@Data
public class ResetPasswordCommand {

    /**
     * 校验身份类型（2手机号 3邮箱）
     */
    private Integer verifyIdentityType;

    /**
     * 校验标识（手机号/邮箱）
     */
    private String identifier;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 组织ID
     */
    private Long estabId;
}
