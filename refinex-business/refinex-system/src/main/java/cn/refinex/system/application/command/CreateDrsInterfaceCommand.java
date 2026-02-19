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
     * HTTP方法
     */
    private String httpMethod;

    /**
     * 接口路径模式
     */
    private String pathPattern;

    /**
     * 权限标识
     */
    private String permissionKey;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;
}
