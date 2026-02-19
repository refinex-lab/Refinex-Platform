package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 创建菜单操作命令
 *
 * @author refinex
 */
@Data
public class CreateMenuOpCommand {

    /**
     * 菜单ID
     */
    private Long menuId;

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
