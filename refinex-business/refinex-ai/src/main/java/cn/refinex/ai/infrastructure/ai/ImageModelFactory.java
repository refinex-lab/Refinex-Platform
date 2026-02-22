package cn.refinex.ai.infrastructure.ai;

import cn.refinex.ai.domain.error.AiErrorCode;
import cn.refinex.ai.domain.model.entity.ModelEntity;
import cn.refinex.ai.domain.model.entity.ModelProvisionEntity;
import cn.refinex.ai.domain.model.entity.ProviderEntity;
import cn.refinex.base.exception.BizException;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.zhipuai.ZhiPuAiImageModel;
import org.springframework.ai.zhipuai.ZhiPuAiImageOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiImageApi;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * ImageModel 工厂
 * <p>
 * 无状态组件，根据供应商编码程序化创建 Spring AI ImageModel 实例。
 *
 * @author refinex
 */
@Component
public class ImageModelFactory {

    /**
     * 根据三层配置创建 ImageModel 实例
     *
     * @param provider        供应商实体
     * @param model           模型实体
     * @param provision       租户模型开通实体
     * @param decryptedApiKey 解密后的 API Key
     * @return ImageModel 实例
     */
    public ImageModel createImageModel(ProviderEntity provider, ModelEntity model,
                                       ModelProvisionEntity provision, String decryptedApiKey) {
        String baseUrl = resolveBaseUrl(provision, provider);
        String modelCode = model.getModelCode();

        return switch (provider.getProviderCode()) {
            case "openai" -> createOpenAiImageModel(baseUrl, decryptedApiKey, modelCode);
            case "zhipu" -> createZhiPuImageModel(baseUrl, decryptedApiKey, modelCode);
            default -> throw new BizException(AiErrorCode.UNSUPPORTED_PROTOCOL);
        };
    }

    /**
     * 创建 OpenAI ImageModel
     *
     * @param baseUrl   API 基础地址
     * @param apiKey    API Key
     * @param modelCode 模型编码
     * @return OpenAiImageModel 实例
     */
    private ImageModel createOpenAiImageModel(String baseUrl, String apiKey, String modelCode) {
        OpenAiImageApi imageApi = OpenAiImageApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        OpenAiImageOptions options = OpenAiImageOptions.builder()
                .model(modelCode)
                .build();
        return new OpenAiImageModel(imageApi, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    /**
     * 创建 ZhiPu ImageModel
     *
     * @param baseUrl   API 基础地址
     * @param apiKey    API Key
     * @param modelCode 模型编码
     * @return ZhiPuAiImageModel 实例
     */
    private ImageModel createZhiPuImageModel(String baseUrl, String apiKey, String modelCode) {
        ZhiPuAiImageApi imageApi = new ZhiPuAiImageApi(apiKey, baseUrl, RestClient.builder());
        ZhiPuAiImageOptions options = ZhiPuAiImageOptions.builder()
                .model(modelCode)
                .build();
        return new ZhiPuAiImageModel(imageApi, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
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
