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
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;
}
