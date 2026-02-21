package cn.refinex.ai.application.command;

import lombok.Data;

/**
 * 创建租户模型开通命令
 *
 * @author refinex
 */
@Data
public class CreateModelProvisionCommand {

    /**
     * 组织ID
     */
    private Long estabId;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * API Key明文（存储时加密）
     */
    private String apiKey;

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
