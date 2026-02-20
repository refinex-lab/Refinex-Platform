package cn.refinex.system.interfaces.vo;

import lombok.Data;

/**
 * 系统用户所属企业视图对象
 *
 * @author refinex
 */
@Data
public class SystemUserEstabVO {

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
     * 是否当前活跃企业
     */
    private Boolean current;
}
