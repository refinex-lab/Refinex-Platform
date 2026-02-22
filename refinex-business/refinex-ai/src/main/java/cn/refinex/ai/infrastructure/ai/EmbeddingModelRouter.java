package cn.refinex.ai.infrastructure.ai;

import cn.refinex.ai.domain.error.AiErrorCode;
import cn.refinex.ai.domain.model.entity.KnowledgeBaseEntity;
import cn.refinex.ai.domain.model.entity.ModelEntity;
import cn.refinex.ai.domain.model.entity.ModelProvisionEntity;
import cn.refinex.ai.domain.model.entity.ProviderEntity;
import cn.refinex.ai.domain.model.enums.ModelType;
import cn.refinex.ai.domain.repository.AiRepository;
import cn.refinex.base.config.RefinexCryptoProperties;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.utils.AesUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

/**
 * EmbeddingModel 路由器
 * <p>
 * 对外统一入口，编排 DB 查询 → 缓存 → 工厂，按 provisionId 或 estabId+modelId 解析 EmbeddingModel。
 *
 * @author refinex
 */
@Component
@RequiredArgsConstructor
public class EmbeddingModelRouter {

    private final AiRepository aiRepository;
    private final EmbeddingModelFactory embeddingModelFactory;
    private final EmbeddingModelRegistry embeddingModelRegistry;
    private final RefinexCryptoProperties cryptoProperties;

    /**
     * 按 provisionId 解析 EmbeddingModel
     *
     * @param provisionId 租户模型开通ID
     * @return EmbeddingModel 实例
     */
    public EmbeddingModel resolve(Long provisionId) {
        EmbeddingModel cached = embeddingModelRegistry.get(provisionId);
        if (cached != null) {
            ModelProvisionEntity provision = aiRepository.findModelProvisionById(provisionId);
            if (provision == null || !isActive(provision)) {
                embeddingModelRegistry.evict(provisionId);
                throw new BizException(AiErrorCode.MODEL_PROVISION_DISABLED);
            }
            return cached;
        }

        return buildAndCache(provisionId);
    }

    /**
     * 按租户+模型解析 EmbeddingModel
     *
     * @param estabId 组织ID
     * @param modelId 模型ID
     * @return EmbeddingModel 实例
     */
    public EmbeddingModel resolve(Long estabId, Long modelId) {
        ModelProvisionEntity provision = aiRepository.findActiveProvision(estabId, modelId);
        if (provision == null) {
            throw new BizException(AiErrorCode.MODEL_PROVISION_NOT_FOUND);
        }
        return resolve(provision.getId());
    }

    /**
     * 解析租户默认嵌入模型
     *
     * @param estabId 组织ID
     * @return EmbeddingModel 实例
     */
    public EmbeddingModel resolveDefault(Long estabId) {
        ModelProvisionEntity provision = aiRepository.findDefaultProvisionByType(estabId, ModelType.EMBEDDING.getCode());
        if (provision == null) {
            throw new BizException(AiErrorCode.DEFAULT_EMBEDDING_MODEL_NOT_CONFIGURED);
        }
        return resolve(provision.getId());
    }

    /**
     * 为知识库解析嵌入模型：KB.embeddingModelId 优先，回退租户默认
     *
     * @param kb 知识库实体
     * @return EmbeddingModel 实例
     */
    public EmbeddingModel resolveForKnowledgeBase(KnowledgeBaseEntity kb) {
        if (kb.getEmbeddingModelId() != null) {
            return resolve(kb.getEstabId(), kb.getEmbeddingModelId());
        }
        return resolveDefault(kb.getEstabId());
    }

    /**
     * 获取知识库对应的嵌入模型 provisionId（用于 VectorStore 缓存 key）
     *
     * @param kb 知识库实体
     * @return 嵌入模型的 provisionId
     */
    public Long resolveProvisionIdForKnowledgeBase(KnowledgeBaseEntity kb) {
        if (kb.getEmbeddingModelId() != null) {
            ModelProvisionEntity provision = aiRepository.findActiveProvision(kb.getEstabId(), kb.getEmbeddingModelId());
            if (provision == null) {
                throw new BizException(AiErrorCode.MODEL_PROVISION_NOT_FOUND);
            }
            return provision.getId();
        }

        ModelProvisionEntity provision = aiRepository.findDefaultProvisionByType(kb.getEstabId(), ModelType.EMBEDDING.getCode());
        if (provision == null) {
            throw new BizException(AiErrorCode.DEFAULT_EMBEDDING_MODEL_NOT_CONFIGURED);
        }
        return provision.getId();
    }

    /**
     * 从 DB 加载三层数据，创建 EmbeddingModel 并写入缓存
     *
     * @param provisionId 租户模型开通ID
     * @return EmbeddingModel 实例
     */
    private EmbeddingModel buildAndCache(Long provisionId) {
        ModelProvisionEntity provision = aiRepository.findModelProvisionById(provisionId);
        if (provision == null || !isActive(provision)) {
            throw new BizException(AiErrorCode.MODEL_PROVISION_DISABLED);
        }

        ModelEntity model = aiRepository.findModelById(provision.getModelId());
        if (model == null || !isEnabled(model.getStatus(), model.getDeleted())) {
            embeddingModelRegistry.evict(provisionId);
            throw new BizException(AiErrorCode.MODEL_DISABLED);
        }

        ProviderEntity provider = aiRepository.findProviderById(model.getProviderId());
        if (provider == null || !isEnabled(provider.getStatus(), provider.getDeleted())) {
            embeddingModelRegistry.evict(provisionId);
            throw new BizException(AiErrorCode.PROVIDER_DISABLED);
        }

        String apiKey = AesUtils.decrypt(provision.getApiKeyCipher(), cryptoProperties.getAesKey());
        if (apiKey == null || apiKey.isBlank()) {
            throw new BizException(AiErrorCode.API_KEY_MISSING);
        }

        EmbeddingModel embeddingModel = embeddingModelFactory.createEmbeddingModel(provider, model, provision, apiKey);
        return embeddingModelRegistry.put(provisionId, embeddingModel);
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
