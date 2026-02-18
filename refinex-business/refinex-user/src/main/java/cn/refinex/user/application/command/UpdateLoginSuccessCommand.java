package cn.refinex.user.application.command;

import lombok.Data;

/**
 * 登录成功更新命令
 *
 * @author refinex
 */
@Data
public class UpdateLoginSuccessCommand {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 身份ID
     */
    private Long identityId;

    /**
     * IP地址
     */
    private String ip;
}
