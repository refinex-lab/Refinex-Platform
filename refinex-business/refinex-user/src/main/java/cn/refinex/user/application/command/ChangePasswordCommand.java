package cn.refinex.user.application.command;

import lombok.Data;

/**
 * 修改密码命令
 *
 * @author refinex
 */
@Data
public class ChangePasswordCommand {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
}
