package cn.refinex.user.application.command;

import lombok.Data;

/**
 * 重置当前用户密码命令
 *
 * @author refinex
 */
@Data
public class ResetCurrentUserPasswordCommand {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 新密码
     */
    private String newPassword;
}
