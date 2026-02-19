package cn.refinex.system.interfaces.vo;

import lombok.Data;

/**
 * 菜单操作管理 VO
 *
 * @author refinex
 */
@Data
public class MenuOpManageVO {

    /**
     * 菜单操作ID
     */
    private Long id;

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
     * 接口路径
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
