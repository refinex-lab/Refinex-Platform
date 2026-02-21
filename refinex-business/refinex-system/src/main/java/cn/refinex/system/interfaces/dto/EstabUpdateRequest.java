package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 企业更新请求
 *
 * @author refinex
 */
@Data
public class EstabUpdateRequest {

    /**
     * 企业名称
     */
    @NotBlank(message = "企业名称不能为空")
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
     * 备注
     */
    private String remark;
}
