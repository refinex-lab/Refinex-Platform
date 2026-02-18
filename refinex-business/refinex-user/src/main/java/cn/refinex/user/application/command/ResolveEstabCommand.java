package cn.refinex.user.application.command;

import lombok.Data;

/**
 * 组织解析命令
 *
 * @author refinex
 */
@Data
public class ResolveEstabCommand {

    /**
     * 组织ID
     */
    private Long estabId;

    /**
     * 组织代码
     */
    private String estabCode;
}
