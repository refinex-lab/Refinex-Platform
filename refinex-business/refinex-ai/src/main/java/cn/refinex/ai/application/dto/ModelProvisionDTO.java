package cn.refinex.ai.application.dto;

import lombok.Data;

/**
 * 租户模型开通 DTO
 *
 * @author refinex
 */
@Data
public class ModelProvisionDTO {

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
     * 供应商ID（关联查询）
     */
    private Long providerId;

    /**
     * 供应商编码（关联查询）
     */
    private String providerCode;

    /**
     * 模型编码（关联查询）
     */
    private String modelCode;

    /**
     * 模型名称（关联查询）
     */
    private String modelName;

    /**
     * API Key密文(AES加密存储)
     */
    private String apiKeyCipher;

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
