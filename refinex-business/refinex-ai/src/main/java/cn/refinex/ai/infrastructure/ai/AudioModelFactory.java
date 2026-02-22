package cn.refinex.ai.infrastructure.ai;

import cn.refinex.ai.domain.error.AiErrorCode;
import cn.refinex.ai.domain.model.entity.ModelEntity;
import cn.refinex.ai.domain.model.entity.ModelProvisionEntity;
import cn.refinex.ai.domain.model.entity.ProviderEntity;
import cn.refinex.base.exception.BizException;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.stereotype.Component;

/**
 * 音频模型工厂
 * <p>
 * 无状态组件，根据供应商编码程序化创建 TTS（TextToSpeechModel）和 STT（TranscriptionModel）实例。
 * TTS 和 STT 共享 OpenAiAudioApi，但分别创建不同的模型实例。
 *
 * @author refinex
 */
@Component
public class AudioModelFactory {

    /**
     * 创建 TTS 模型实例
     *
     * @param provider        供应商实体
     * @param model           模型实体
     * @param provision       租户模型开通实体
     * @param decryptedApiKey 解密后的 API Key
     * @return TextToSpeechModel 实例
     */
    public TextToSpeechModel createSpeechModel(ProviderEntity provider, ModelEntity model,
                                                ModelProvisionEntity provision, String decryptedApiKey) {
        String baseUrl = resolveBaseUrl(provision, provider);
        String modelCode = model.getModelCode();

        return switch (provider.getProviderCode()) {
            case "openai" -> createOpenAiSpeechModel(baseUrl, decryptedApiKey, modelCode);
            default -> throw new BizException(AiErrorCode.UNSUPPORTED_PROTOCOL);
        };
    }

    /**
     * 创建 STT 模型实例
     *
     * @param provider        供应商实体
     * @param model           模型实体
     * @param provision       租户模型开通实体
     * @param decryptedApiKey 解密后的 API Key
     * @return TranscriptionModel 实例
     */
    public TranscriptionModel createTranscriptionModel(ProviderEntity provider, ModelEntity model,
                                                        ModelProvisionEntity provision, String decryptedApiKey) {
        String baseUrl = resolveBaseUrl(provision, provider);
        String modelCode = model.getModelCode();

        return switch (provider.getProviderCode()) {
            case "openai" -> createOpenAiTranscriptionModel(baseUrl, decryptedApiKey, modelCode);
            default -> throw new BizException(AiErrorCode.UNSUPPORTED_PROTOCOL);
        };
    }

    /**
     * 创建 OpenAI TTS 模型
     *
     * @param baseUrl   API 基础地址
     * @param apiKey    API Key
     * @param modelCode 模型编码
     * @return OpenAiAudioSpeechModel 实例
     */
    private TextToSpeechModel createOpenAiSpeechModel(String baseUrl, String apiKey, String modelCode) {
        OpenAiAudioApi audioApi = OpenAiAudioApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                .model(modelCode)
                .build();
        return new OpenAiAudioSpeechModel(audioApi, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    /**
     * 创建 OpenAI STT 模型
     *
     * @param baseUrl   API 基础地址
     * @param apiKey    API Key
     * @param modelCode 模型编码
     * @return OpenAiAudioTranscriptionModel 实例
     */
    private TranscriptionModel createOpenAiTranscriptionModel(String baseUrl, String apiKey, String modelCode) {
        OpenAiAudioApi audioApi = OpenAiAudioApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .model(modelCode)
                .build();
        return new OpenAiAudioTranscriptionModel(audioApi, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
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
