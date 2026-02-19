package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 企业列表查询命令
 *
 * @author refinex
 */
@Data
public class QueryEstabListCommand {

    /**
     * 状态
     */
    private Integer status;

    /**
     * 企业类型
     */
    private Integer estabType;

    /**
     * 关键字
     */
    private String keyword;
}
