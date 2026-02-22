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
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.stereotype.Component;

/**
 * TranscriptionModel 路由器
 * <p>
 * 对外统一入口，编排 DB 查询 → 缓存 → 工厂，按 provisionId 或 estabId+modelId 解析 TranscriptionModel。
 *
 * @author refinex
 */
@Component
@RequiredArgsConstructor
public class TranscriptionModelRouter {

    private final AiRepository aiRepository;
    private final AudioModelFactory audioModelFactory;
    private final TranscriptionModelRegistry transcriptionModelRegistry;
    private final RefinexCryptoProperties cryptoProperties;

    /**
     * 按 provisionId 解析 TranscriptionModel
     *
     * @param provisionId 租户模型开通ID
     * @return TranscriptionModel 实例
     */
    public TranscriptionModel resolve(Long provisionId) {
        TranscriptionModel cached = transcriptionModelRegistry.get(provisionId);
        if (cached != null) {
            ModelProvisionEntity provision = aiRepository.findModelProvisionById(provisionId);
            if (provision == null || !isActive(provision)) {
                transcriptionModelRegistry.evict(provisionId);
                throw new BizException(AiErrorCode.MODEL_PROVISION_DISABLED);
            }
            return cached;
        }

        return buildAndCache(provisionId);
    }

    /**
     * 按租户+模型解析 TranscriptionModel
     *
     * @param estabId 组织ID
     * @param modelId 模型ID
     * @return TranscriptionModel 实例
     */
    public TranscriptionModel resolve(Long estabId, Long modelId) {
        ModelProvisionEntity provision = aiRepository.findActiveProvision(estabId, modelId);
        if (provision == null) {
            throw new BizException(AiErrorCode.MODEL_PROVISION_NOT_FOUND);
        }
        return resolve(provision.getId());
    }

    /**
     * 解析租户默认 STT 模型
     *
     * @param estabId 组织ID
     * @return TranscriptionModel 实例
     */
    public TranscriptionModel resolveDefault(Long estabId) {
        ModelProvisionEntity provision = aiRepository.findDefaultProvisionByType(estabId, 4);
        if (provision == null) {
            throw new BizException(AiErrorCode.DEFAULT_MODEL_NOT_CONFIGURED);
        }
        return resolve(provision.getId());
    }

    /**
     * 从 DB 加载三层数据，创建 TranscriptionModel 并写入缓存
     *
     * @param provisionId 租户模型开通ID
     * @return TranscriptionModel 实例
     */
    private TranscriptionModel buildAndCache(Long provisionId) {
        ModelProvisionEntity provision = aiRepository.findModelProvisionById(provisionId);
        if (provision == null || !isActive(provision)) {
            throw new BizException(AiErrorCode.MODEL_PROVISION_DISABLED);
        }

        ModelEntity model = aiRepository.findModelById(provision.getModelId());
        if (model == null || !isEnabled(model.getStatus(), model.getDeleted())) {
            transcriptionModelRegistry.evict(provisionId);
            throw new BizException(AiErrorCode.MODEL_DISABLED);
        }

        ProviderEntity provider = aiRepository.findProviderById(model.getProviderId());
        if (provider == null || !isEnabled(provider.getStatus(), provider.getDeleted())) {
            transcriptionModelRegistry.evict(provisionId);
            throw new BizException(AiErrorCode.PROVIDER_DISABLED);
        }

        String apiKey = AesUtils.decrypt(provision.getApiKeyCipher(), cryptoProperties.getAesKey());
        if (apiKey == null || apiKey.isBlank()) {
            throw new BizException(AiErrorCode.API_KEY_MISSING);
        }

        TranscriptionModel transcriptionModel = audioModelFactory.createTranscriptionModel(provider, model, provision, apiKey);
        return transcriptionModelRegistry.put(provisionId, transcriptionModel);
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
