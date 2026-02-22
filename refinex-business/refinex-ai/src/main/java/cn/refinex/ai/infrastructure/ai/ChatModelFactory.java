package cn.refinex.ai.infrastructure.ai;

import cn.refinex.ai.domain.error.AiErrorCode;
import cn.refinex.ai.domain.model.entity.ModelEntity;
import cn.refinex.ai.domain.model.entity.ModelProvisionEntity;
import cn.refinex.ai.domain.model.entity.ProviderEntity;
import cn.refinex.base.exception.BizException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.stereotype.Component;

/**
 * ChatModel 工厂
 * <p>
 * 无状态组件，根据供应商协议和编码程序化创建 Spring AI ChatModel 实例。
 *
 * @author refinex
 */
@Component
public class ChatModelFactory {

    /**
     * 根据三层配置创建 ChatModel 实例
     *
     * @param provider       供应商实体
     * @param model          模型实体
     * @param provision      租户模型开通实体
     * @param decryptedApiKey 解密后的 API Key
     * @return ChatModel 实例
     */
    public ChatModel createChatModel(ProviderEntity provider, ModelEntity model,
                                     ModelProvisionEntity provision, String decryptedApiKey) {
        String baseUrl = resolveBaseUrl(provision, provider);
        String modelCode = model.getModelCode();
        String protocol = provider.getProtocol();

        return switch (protocol) {
            case "openai" -> createOpenAiProtocolModel(provider.getProviderCode(), baseUrl, decryptedApiKey, modelCode);
            case "anthropic" -> createAnthropicModel(baseUrl, decryptedApiKey, modelCode);
            default -> throw new BizException(AiErrorCode.UNSUPPORTED_PROTOCOL);
        };
    }

    /**
     * 创建 OpenAI 协议族模型（OpenAI / DeepSeek / ZhiPu / MiniMax / 其他兼容）
     *
     * @param providerCode   供应商编码
     * @param baseUrl        API 基础地址
     * @param apiKey         API Key
     * @param modelCode      模型编码
     * @return ChatModel 实例
     */
    private ChatModel createOpenAiProtocolModel(String providerCode, String baseUrl, String apiKey, String modelCode) {
        return switch (providerCode) {
            case "openai" -> createOpenAiModel(baseUrl, apiKey, modelCode);
            case "deepseek" -> createDeepSeekModel(baseUrl, apiKey, modelCode);
            case "zhipu" -> createZhiPuModel(baseUrl, apiKey, modelCode);
            case "minimax" -> createMiniMaxModel(baseUrl, apiKey, modelCode);
            default -> createOpenAiModel(baseUrl, apiKey, modelCode); // 兼容 OpenAI 协议的第三方
        };
    }

    /**
     * 创建 OpenAI ChatModel
     *
     * @param baseUrl   API 基础地址
     * @param apiKey    API Key
     * @param modelCode 模型编码
     * @return OpenAiChatModel 实例
     */
    private ChatModel createOpenAiModel(String baseUrl, String apiKey, String modelCode) {
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(modelCode)
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(options)
                .build();
    }

    /**
     * 创建 DeepSeek ChatModel
     *
     * @param baseUrl   API 基础地址
     * @param apiKey    API Key
     * @param modelCode 模型编码
     * @return DeepSeekChatModel 实例
     */
    private ChatModel createDeepSeekModel(String baseUrl, String apiKey, String modelCode) {
        DeepSeekApi api = DeepSeekApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        return DeepSeekChatModel.builder()
                .deepSeekApi(api)
                .defaultOptions(org.springframework.ai.deepseek.DeepSeekChatOptions.builder()
                        .model(modelCode)
                        .build())
                .build();
    }

    /**
     * 创建 ZhiPu ChatModel
     *
     * @param baseUrl   API 基础地址
     * @param apiKey    API Key
     * @param modelCode 模型编码
     * @return ZhiPuAiChatModel 实例
     */
    private ChatModel createZhiPuModel(String baseUrl, String apiKey, String modelCode) {
        ZhiPuAiApi api = ZhiPuAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        return new ZhiPuAiChatModel(api,
                org.springframework.ai.zhipuai.ZhiPuAiChatOptions.builder()
                        .model(modelCode)
                        .build());
    }

    /**
     * 创建 MiniMax ChatModel
     *
     * @param baseUrl   API 基础地址
     * @param apiKey    API Key
     * @param modelCode 模型编码
     * @return MiniMaxChatModel 实例
     */
    private ChatModel createMiniMaxModel(String baseUrl, String apiKey, String modelCode) {
        MiniMaxApi api = new MiniMaxApi(baseUrl, apiKey);
        return new MiniMaxChatModel(api,
                org.springframework.ai.minimax.MiniMaxChatOptions.builder()
                        .model(modelCode)
                        .build());
    }

    /**
     * 创建 Anthropic ChatModel
     *
     * @param baseUrl   API 基础地址
     * @param apiKey    API Key
     * @param modelCode 模型编码
     * @return AnthropicChatModel 实例
     */
    private ChatModel createAnthropicModel(String baseUrl, String apiKey, String modelCode) {
        AnthropicApi api = AnthropicApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        return AnthropicChatModel.builder()
                .anthropicApi(api)
                .defaultOptions(org.springframework.ai.anthropic.AnthropicChatOptions.builder()
                        .model(modelCode)
                        .build())
                .build();
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
