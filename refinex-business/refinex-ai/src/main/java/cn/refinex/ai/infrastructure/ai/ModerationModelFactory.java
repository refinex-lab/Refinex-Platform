package cn.refinex.ai.infrastructure.ai;

import cn.refinex.ai.domain.error.AiErrorCode;
import cn.refinex.ai.domain.model.entity.ModelEntity;
import cn.refinex.ai.domain.model.entity.ModelProvisionEntity;
import cn.refinex.ai.domain.model.entity.ProviderEntity;
import cn.refinex.ai.domain.model.enums.ProviderProtocol;
import cn.refinex.base.exception.BizException;
import org.springframework.ai.moderation.ModerationModel;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.ai.openai.OpenAiModerationOptions;
import org.springframework.ai.openai.api.OpenAiModerationApi;
import org.springframework.stereotype.Component;

/**
 * ModerationModel 工厂
 * <p>
 * 无状态组件，根据供应商协议程序化创建 Spring AI ModerationModel 实例。
 * 当前仅支持 OpenAI 协议（Spring AI 1.1.2 只有 OpenAiModerationModel 实现）。
 *
 * @author refinex
 */
@Component
public class ModerationModelFactory {

    /**
     * 根据三层配置创建 ModerationModel 实例
     *
     * @param provider        供应商实体
     * @param model           模型实体
     * @param provision       租户模型开通实体
     * @param decryptedApiKey 解密后的 API Key
     * @return ModerationModel 实例
     */
    public ModerationModel createModerationModel(ProviderEntity provider, ModelEntity model, ModelProvisionEntity provision, String decryptedApiKey) {
        String baseUrl = resolveBaseUrl(provision, provider);
        String modelCode = model.getModelCode();
        ProviderProtocol protocol = ProviderProtocol.fromCode(provider.getProtocol());
        if (protocol == null) {
            throw new BizException(AiErrorCode.UNSUPPORTED_PROTOCOL);
        }

        return switch (protocol) {
            case OPENAI -> createOpenAiModerationModel(baseUrl, decryptedApiKey, modelCode);
            case ANTHROPIC, OLLAMA -> throw new BizException(AiErrorCode.UNSUPPORTED_PROTOCOL);
        };
    }

    /**
     * 创建 OpenAI ModerationModel
     *
     * @param baseUrl   API 基础地址
     * @param apiKey    API Key
     * @param modelCode 模型编码
     * @return OpenAiModerationModel 实例
     */
    private ModerationModel createOpenAiModerationModel(String baseUrl, String apiKey, String modelCode) {
        OpenAiModerationApi api = OpenAiModerationApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        OpenAiModerationModel model = new OpenAiModerationModel(api);
        return model.withDefaultOptions(OpenAiModerationOptions.builder().model(modelCode).build());
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
