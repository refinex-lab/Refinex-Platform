package cn.refinex.user.application.dto;

import lombok.Data;

/**
 * 用户所属企业 DTO
 *
 * @author refinex
 */
@Data
public class UserEstabDTO {

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 企业编码
     */
    private String estabCode;

    /**
     * 企业名称
     */
    private String estabName;

    /**
     * 企业简称
     */
    private String estabShortName;

    /**
     * 企业Logo
     */
    private String logoUrl;

    /**
     * 企业类型
     */
    private Integer estabType;

    /**
     * 是否企业管理员
     */
    private Boolean admin;

    /**
     * 是否当前企业
     */
    private Boolean current;
}
