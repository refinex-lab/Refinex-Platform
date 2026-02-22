package cn.refinex.ai.infrastructure.ai;

import cn.refinex.ai.domain.error.AiErrorCode;
import cn.refinex.ai.domain.model.entity.ModelEntity;
import cn.refinex.ai.domain.model.entity.ModelProvisionEntity;
import cn.refinex.ai.domain.model.entity.ProviderEntity;
import cn.refinex.ai.domain.model.enums.ProviderProtocol;
import cn.refinex.base.exception.BizException;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.minimax.MiniMaxEmbeddingModel;
import org.springframework.ai.minimax.MiniMaxEmbeddingOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingModel;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.stereotype.Component;

/**
 * EmbeddingModel 工厂
 * <p>
 * 无状态组件，根据供应商协议和编码程序化创建 Spring AI EmbeddingModel 实例。
 *
 * @author refinex
 */
@Component
public class EmbeddingModelFactory {

    /**
     * 根据三层配置创建 EmbeddingModel 实例
     *
     * @param provider        供应商实体
     * @param model           模型实体
     * @param provision       租户模型开通实体
     * @param decryptedApiKey 解密后的 API Key
     * @return EmbeddingModel 实例
     */
    public EmbeddingModel createEmbeddingModel(ProviderEntity provider, ModelEntity model, ModelProvisionEntity provision, String decryptedApiKey) {
        String baseUrl = resolveBaseUrl(provision, provider);
        String modelCode = model.getModelCode();
        ProviderProtocol protocol = ProviderProtocol.fromCode(provider.getProtocol());
        if (protocol == null) {
            throw new BizException(AiErrorCode.UNSUPPORTED_PROTOCOL);
        }

        return switch (protocol) {
            case OPENAI ->
                    createOpenAiProtocolEmbedding(provider.getProviderCode(), baseUrl, decryptedApiKey, modelCode);
            case ANTHROPIC -> throw new BizException(AiErrorCode.UNSUPPORTED_PROTOCOL); // Anthropic 无嵌入 API
            case OLLAMA -> throw new BizException(AiErrorCode.UNSUPPORTED_PROTOCOL);    // TODO: Ollama 支持
        };
    }

    /**
     * 创建 OpenAI 协议族嵌入模型（OpenAI / ZhiPu / MiniMax / 其他兼容）
     *
     * @param providerCode 供应商编码
     * @param baseUrl      API 基础地址
     * @param apiKey       API Key
     * @param modelCode    模型编码
     * @return EmbeddingModel 实例
     */
    private EmbeddingModel createOpenAiProtocolEmbedding(String providerCode, String baseUrl, String apiKey, String modelCode) {
        return switch (providerCode) {
            case "openai" -> createOpenAiEmbedding(baseUrl, apiKey, modelCode);
            case "zhipu" -> createZhiPuEmbedding(baseUrl, apiKey, modelCode);
            case "minimax" -> createMiniMaxEmbedding(baseUrl, apiKey, modelCode);
            default -> createOpenAiEmbedding(baseUrl, apiKey, modelCode); // 兼容 OpenAI 协议的第三方
        };
    }

    /**
     * 创建 OpenAI EmbeddingModel
     *
     * @param baseUrl   API 基础地址
     * @param apiKey    API Key
     * @param modelCode 模型编码
     * @return OpenAiEmbeddingModel 实例
     */
    private EmbeddingModel createOpenAiEmbedding(String baseUrl, String apiKey, String modelCode) {
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model(modelCode)
                .build();

        return new OpenAiEmbeddingModel(api, MetadataMode.EMBED, options);
    }

    /**
     * 创建 ZhiPu EmbeddingModel
     *
     * @param baseUrl   API 基础地址
     * @param apiKey    API Key
     * @param modelCode 模型编码
     * @return ZhiPuAiEmbeddingModel 实例
     */
    private EmbeddingModel createZhiPuEmbedding(String baseUrl, String apiKey, String modelCode) {
        ZhiPuAiApi api = ZhiPuAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        return new ZhiPuAiEmbeddingModel(api, MetadataMode.EMBED, ZhiPuAiEmbeddingOptions.builder()
                .model(modelCode)
                .build());
    }

    /**
     * 创建 MiniMax EmbeddingModel
     *
     * @param baseUrl   API 基础地址
     * @param apiKey    API Key
     * @param modelCode 模型编码
     * @return MiniMaxEmbeddingModel 实例
     */
    private EmbeddingModel createMiniMaxEmbedding(String baseUrl, String apiKey, String modelCode) {
        MiniMaxApi api = new MiniMaxApi(baseUrl, apiKey);

        return new MiniMaxEmbeddingModel(api, MetadataMode.EMBED, MiniMaxEmbeddingOptions.builder()
                .model(modelCode)
                .build());
    }

    /**
     * 解析 API 基础地址，租户覆盖优先于供应商默认
     *
     * @param provision 租户模型开通实体
     * @param provider  供应商实体
     * @return API 基础地址
     */
    private String resolveBaseUrl(ModelProvisionEntity provision, ProviderEntity provider) {
        if (provision.getApiBaseUrl() != null && !provision.getApiBaseUrl().isBlank()) {
            return provision.getApiBaseUrl();
        }
        return provider.getBaseUrl();
    }
}
