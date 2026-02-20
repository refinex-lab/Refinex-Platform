package cn.refinex.system.application.dto;

import lombok.Data;

/**
 * 菜单 DTO
 *
 * @author refinex
 */
@Data
public class MenuDTO {

    /**
     * 菜单ID
     */
    private Long id;

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 系统ID
     */
    private Long systemId;

    /**
     * 父级菜单ID
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
     * 菜单类型 0目录 1菜单
     */
    private Integer menuType;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 图标
     */
    private String icon;

    /**
     * 是否内建菜单 1是 0否
     */
    private Integer isBuiltin;

    /**
     * 是否可见 1可见 0隐藏
     */
    private Integer visible;

    /**
     * 是否外链 1是 0否
     */
    private Integer isFrame;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 排序(升序)
     */
    private Integer sort;
}
