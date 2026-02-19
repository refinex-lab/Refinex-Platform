package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 查询数据资源接口列表命令
 *
 * @author refinex
 */
@Data
public class QueryDrsInterfaceListCommand {

    /**
     * 数据资源ID
     */
    private Long drsId;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 关键字（接口编码/名称）
     */
    private String keyword;
}
