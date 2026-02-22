package cn.refinex.ai.infrastructure.ai;

import cn.refinex.ai.domain.error.AiErrorCode;
import cn.refinex.ai.domain.model.entity.ModelEntity;
import cn.refinex.ai.domain.model.entity.ModelProvisionEntity;
import cn.refinex.ai.domain.model.entity.ProviderEntity;
import cn.refinex.ai.domain.repository.AiRepository;
import cn.refinex.base.config.RefinexCryptoProperties;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.utils.AesUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * ChatModel 路由器
 * <p>
 * 对外统一入口，编排 DB 查询 → 缓存 → 工厂，按 provisionId 或 estabId+modelId 解析 ChatModel。
 *
 * @author refinex
 */
@Component
@RequiredArgsConstructor
public class ChatModelRouter {

    private final AiRepository aiRepository;
    private final ChatModelFactory chatModelFactory;
    private final ChatModelRegistry chatModelRegistry;
    private final RefinexCryptoProperties cryptoProperties;

    /**
     * 按 provisionId 解析 ChatModel
     *
     * @param provisionId 租户模型开通ID
     * @return ChatModel 实例
     */
    public ChatModel resolve(Long provisionId) {
        ChatModel cached = chatModelRegistry.get(provisionId);
        if (cached != null) {
            // 校验 provision 仍为启用状态
            ModelProvisionEntity provision = aiRepository.findModelProvisionById(provisionId);
            if (provision == null || !isActive(provision)) {
                chatModelRegistry.evict(provisionId);
                throw new BizException(AiErrorCode.MODEL_PROVISION_DISABLED);
            }
            return cached;
        }

        return buildAndCache(provisionId);
    }

    /**
     * 按租户+模型解析 ChatModel
     *
     * @param estabId 组织ID
     * @param modelId 模型ID
     * @return ChatModel 实例
     */
    public ChatModel resolve(Long estabId, Long modelId) {
        ModelProvisionEntity provision = aiRepository.findActiveProvision(estabId, modelId);
        if (provision == null) {
            throw new BizException(AiErrorCode.MODEL_PROVISION_NOT_FOUND);
        }
        return resolve(provision.getId());
    }

    /**
     * 按租户默认模型解析 ChatModel
     *
     * @param estabId 组织ID
     * @return ChatModel 实例
     */
    public ChatModel resolveDefault(Long estabId) {
        ModelProvisionEntity provision = aiRepository.findDefaultProvision(estabId);
        if (provision == null) {
            throw new BizException(AiErrorCode.DEFAULT_MODEL_NOT_CONFIGURED);
        }
        return resolve(provision.getId());
    }

    /**
     * 从 DB 加载三层数据，创建 ChatModel 并写入缓存
     *
     * @param provisionId 租户模型开通ID
     * @return ChatModel 实例
     */
    private ChatModel buildAndCache(Long provisionId) {
        // 加载 provision
        ModelProvisionEntity provision = aiRepository.findModelProvisionById(provisionId);
        if (provision == null || !isActive(provision)) {
            throw new BizException(AiErrorCode.MODEL_PROVISION_DISABLED);
        }

        // 加载 model
        ModelEntity model = aiRepository.findModelById(provision.getModelId());
        if (model == null || !isEnabled(model.getStatus(), model.getDeleted())) {
            chatModelRegistry.evict(provisionId);
            throw new BizException(AiErrorCode.MODEL_DISABLED);
        }

        // 加载 provider
        ProviderEntity provider = aiRepository.findProviderById(model.getProviderId());
        if (provider == null || !isEnabled(provider.getStatus(), provider.getDeleted())) {
            chatModelRegistry.evict(provisionId);
            throw new BizException(AiErrorCode.PROVIDER_DISABLED);
        }

        // 解密 API Key
        String apiKey = AesUtils.decrypt(provision.getApiKeyCipher(), cryptoProperties.getAesKey());
        if (apiKey == null || apiKey.isBlank()) {
            throw new BizException(AiErrorCode.API_KEY_MISSING);
        }

        // 创建并缓存
        ChatModel chatModel = chatModelFactory.createChatModel(provider, model, provision, apiKey);
        return chatModelRegistry.put(provisionId, chatModel);
    }

    /**
     * 判断 provision 是否活跃（status=1, deleted=0）
     *
     * @param provision 租户模型开通实体
     * @return 是否活跃
     */
    private boolean isActive(ModelProvisionEntity provision) {
        return isEnabled(provision.getStatus(), provision.getDeleted());
    }

    /**
     * 判断实体是否启用（status=1, deleted=0）
     *
     * @param status  状态
     * @param deleted 删除标记
     * @return 是否启用
     */
    private boolean isEnabled(Integer status, Integer deleted) {
        return (status != null && status == 1) && (deleted == null || deleted == 0);
    }
}
