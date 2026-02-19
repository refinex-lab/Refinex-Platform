package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 查询系统列表命令
 *
 * @author refinex
 */
@Data
public class QuerySystemListCommand {

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 关键字（系统编码/名称）
     */
    private String keyword;
}
