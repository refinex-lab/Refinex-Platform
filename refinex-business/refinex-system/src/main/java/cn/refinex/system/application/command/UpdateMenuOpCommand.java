package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 更新菜单操作命令
 *
 * @author refinex
 */
@Data
public class UpdateMenuOpCommand {

    /**
     * 菜单操作ID
     */
    private Long menuOpId;

    /**
     * 操作编码
     */
    private String opCode;

    /**
     * 操作名称
     */
    private String opName;

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
