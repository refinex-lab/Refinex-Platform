package cn.refinex.ai.domain.model.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 租户模型开通配置领域实体
 *
 * @author refinex
 */
@Data
public class ModelProvisionEntity {

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

    /**
     * 逻辑删除 0未删 1已删
     */
    private Integer deleted;
}
