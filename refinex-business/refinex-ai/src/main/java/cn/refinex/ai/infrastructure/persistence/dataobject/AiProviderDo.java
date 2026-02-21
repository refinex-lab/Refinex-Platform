package cn.refinex.ai.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI供应商定义 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_provider")
public class AiProviderDo extends BaseEntity {

    /**
     * 供应商编码(如 openai/anthropic/deepseek/zhipu/minimax)
     */
    private String providerCode;

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
