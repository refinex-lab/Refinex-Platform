package cn.refinex.system.application.dto;

import lombok.Data;

/**
 * 系统信息 DTO
 *
 * @author refinex
 */
@Data
public class SystemDTO {

    /**
     * 系统ID
     */
    private Long id;

    /**
     * 系统编码
     */
    private String systemCode;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 系统类型 0平台 1租户 2业务子系统
     */
    private Integer systemType;

    /**
     * 系统基础URL
     */
    private String baseUrl;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 排序(升序)
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;
}
