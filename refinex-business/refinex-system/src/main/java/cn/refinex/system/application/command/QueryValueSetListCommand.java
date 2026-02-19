package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 查询值集列表命令
 *
 * @author refinex
 */
@Data
public class QueryValueSetListCommand {

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 关键字（值集编码/名称）
     */
    private String keyword;
}
