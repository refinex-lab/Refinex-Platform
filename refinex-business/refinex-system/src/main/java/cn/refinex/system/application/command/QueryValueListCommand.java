package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 查询值集明细列表命令
 *
 * @author refinex
 */
@Data
public class QueryValueListCommand {

    /**
     * 值集编码
     */
    private String setCode;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 关键字（值编码/名称）
     */
    private String keyword;
}
