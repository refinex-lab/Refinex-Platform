package cn.refinex.system.application.dto;

import lombok.Data;

/**
 * 菜单操作管理 DTO
 *
 * @author refinex
 */
@Data
public class MenuOpManageDTO {

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
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;
}
