package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 创建数据资源接口命令
 *
 * @author refinex
 */
@Data
public class CreateDrsInterfaceCommand {

    /**
     * 数据资源ID
     */
    private Long drsId;

    /**
     * 接口编码
     */
    private String interfaceCode;

    /**
     * 接口名称
     */
    private String interfaceName;

    /**
     * 数据资源SQL
     */
    private String interfaceSql;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;
}
