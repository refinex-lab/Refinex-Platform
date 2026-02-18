package cn.refinex.user.application.command;

import lombok.Data;

/**
 * 用户信息查询命令
 *
 * @author refinex
 */
@Data
public class QueryUserInfoCommand {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 团队ID
     */
    private Long estabId;
}
