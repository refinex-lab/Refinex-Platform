package cn.refinex.user.application.command;

import lombok.Data;

/**
 * 登录失败更新命令
 *
 * @author refinex
 */
@Data
public class UpdateLoginFailureCommand {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 登录失败阈值
     */
    private Integer threshold;

    /**
     * 登录失败锁定分钟数
     */
    private Integer lockMinutes;
}
