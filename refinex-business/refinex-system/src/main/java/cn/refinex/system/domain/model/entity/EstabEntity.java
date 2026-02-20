package cn.refinex.system.domain.model.entity;

import lombok.Data;

/**
 * 企业实体
 *
 * @author refinex
 */
@Data
public class EstabEntity {

    /**
     * 企业ID
     */
    private Long id;

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
     * 企业类型
     */
    private Integer estabType;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 统一社会信用代码
     */
    private String creditCode;

    /**
     * 行业编码
     */
    private String industryCode;

    /**
     * 规模区间
     */
    private String sizeRange;

    /**
     * 负责人用户ID
     */
    private Long ownerUserId;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 官网地址
     */
    private String websiteUrl;

    /**
     * Logo 地址
     */
    private String logoUrl;

    /**
     * 营业执照图片地址
     */
    private String licenseUrl;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否删除
     */
    private Integer deleted;
}
