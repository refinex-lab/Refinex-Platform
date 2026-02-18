package cn.refinex.user.application.command;

import lombok.Data;

/**
 * 登录主体查询命令
 *
 * @author refinex
 */
@Data
public class QueryAuthSubjectCommand {

    /**
     * 身份类型
     */
    private Integer identityType;

    /**
     * 标识
     */
    private String identifier;

    /**
     * 团队ID
     */
    private Long estabId;
}
