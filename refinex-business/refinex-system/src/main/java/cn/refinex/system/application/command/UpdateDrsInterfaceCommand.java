package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 更新数据资源接口命令
 *
 * @author refinex
 */
@Data
public class UpdateDrsInterfaceCommand {

    /**
     * 接口ID
     */
    private Long interfaceId;

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
