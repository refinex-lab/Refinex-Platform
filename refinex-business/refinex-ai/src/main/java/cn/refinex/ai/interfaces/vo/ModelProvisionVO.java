package cn.refinex.ai.interfaces.vo;

import lombok.Data;

/**
 * 租户模型开通 VO
 *
 * @author refinex
 */
@Data
public class ModelProvisionVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 组织ID
     */
    private Long estabId;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * API Key脱敏显示（仅显示后4位）
     */
    private String apiKeyMasked;

    /**
     * 自定义API地址(覆盖供应商默认)
     */
    private String apiBaseUrl;

    /**
     * 日调用额度(NULL不限)
     */
    private Integer dailyQuota;

    /**
     * 月调用额度(NULL不限)
     */
    private Integer monthlyQuota;

    /**
     * 是否该租户默认模型 1是 0否
     */
    private Integer isDefault;

    /**
     * 状态 1启用 0停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展信息(如自定义请求头)
     */
    private String extJson;
}
