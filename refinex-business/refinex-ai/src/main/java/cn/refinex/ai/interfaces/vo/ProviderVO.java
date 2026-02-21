package cn.refinex.ai.interfaces.vo;

import lombok.Data;

/**
 * 供应商 VO
 *
 * @author refinex
 */
@Data
public class ProviderVO {

    /**
     * 提供商ID
     */
    private Long id;

    /**
     * 供应商编码(如 openai/anthropic/deepseek/zhipu/minimax)
     */
    private String providerCode;

    /**
     * 提供商名称，用于显示
     */
    private String providerName;

    /**
     * 接口协议(openai/anthropic/ollama)
     */
    private String protocol;

    /**
     * 默认 API 基础地址
     */
    private String baseUrl;

    /**
     * 提供商图标URL
     */
    private String iconUrl;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 排序值，数值越小排序越靠前
     */
    private Integer sort;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 扩展信息，JSON格式存储
     */
    private String extJson;
}
