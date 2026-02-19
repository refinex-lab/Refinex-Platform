package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 更新菜单命令
 *
 * @author refinex
 */
@Data
public class UpdateMenuCommand {

    /**
     * 菜单ID
     */
    private Long menuId;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单编码
     */
    private String menuCode;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单类型 0目录 1菜单 2按钮
     */
    private Integer menuType;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 前端组件
     */
    private String component;

    /**
     * 权限标识
     */
    private String permissionKey;

    /**
     * 图标
     */
    private String icon;

    /**
     * 是否可见 1可见 0隐藏
     */
    private Integer visible;

    /**
     * 是否外链 1是 0否
     */
    private Integer isFrame;

    /**
     * 是否缓存 1是 0否
     */
    private Integer isCache;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;
}
