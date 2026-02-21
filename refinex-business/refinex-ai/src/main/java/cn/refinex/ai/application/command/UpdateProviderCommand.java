package cn.refinex.ai.application.command;

import lombok.Data;

/**
 * 更新供应商命令
 *
 * @author refinex
 */
@Data
public class UpdateProviderCommand {

    /**
     * 主键ID
     */
    private Long providerId;

    /**
     * 供应商名称
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
     * 供应商图标地址
     */
    private String iconUrl;

    /**
     * 状态 1启用 0停用
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

    /**
     * 扩展信息
     */
    private String extJson;
}
