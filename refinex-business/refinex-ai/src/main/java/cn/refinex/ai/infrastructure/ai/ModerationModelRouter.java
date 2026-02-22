package cn.refinex.ai.infrastructure.ai;

import cn.refinex.ai.domain.error.AiErrorCode;
import cn.refinex.ai.domain.model.entity.ModelEntity;
import cn.refinex.ai.domain.model.entity.ModelProvisionEntity;
import cn.refinex.ai.domain.model.entity.ProviderEntity;
import cn.refinex.ai.domain.model.enums.ModelType;
import cn.refinex.ai.domain.repository.AiRepository;
import cn.refinex.base.config.RefinexCryptoProperties;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.utils.AesUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.moderation.ModerationModel;
import org.springframework.stereotype.Component;

/**
 * ModerationModel 路由器
 * <p>
 * 对外统一入口，编排 DB 查询 → 缓存 → 工厂，按 provisionId 或 estabId 解析 ModerationModel。
 * <p>
 * 与其他 Router 的关键区别：{@link #resolveDefaultOrNull(Long)} 未配置时返回 null 而非抛异常，
 * 因为内容审核是可选功能。
 *
 * @author refinex
 */
@Component
@RequiredArgsConstructor
public class ModerationModelRouter {

    private final AiRepository aiRepository;
    private final ModerationModelFactory moderationModelFactory;
    private final ModerationModelRegistry moderationModelRegistry;
    private final RefinexCryptoProperties cryptoProperties;

    /**
     * 按 provisionId 解析 ModerationModel
     *
     * @param provisionId 租户模型开通ID
     * @return ModerationModel 实例
     */
    public ModerationModel resolve(Long provisionId) {
        ModerationModel cached = moderationModelRegistry.get(provisionId);
        if (cached != null) {
            ModelProvisionEntity provision = aiRepository.findModelProvisionById(provisionId);
            if (provision == null || !isActive(provision)) {
                moderationModelRegistry.evict(provisionId);
                throw new BizException(AiErrorCode.MODEL_PROVISION_DISABLED);
            }
            return cached;
        }

        return buildAndCache(provisionId);
    }

    /**
     * 解析租户默认内容审核模型，未配置时返回 null（审核为可选功能）
     *
     * @param estabId 组织ID
     * @return ModerationModel 实例，未配置返回 null
     */
    public ModerationModel resolveDefaultOrNull(Long estabId) {
        ModelProvisionEntity provision = aiRepository.findDefaultProvisionByType(estabId, ModelType.MODERATION.getCode());
        if (provision == null) {
            return null;
        }
        return resolve(provision.getId());
    }

    /**
     * 从 DB 加载三层数据，创建 ModerationModel 并写入缓存
     *
     * @param provisionId 租户模型开通ID
     * @return ModerationModel 实例
     */
    private ModerationModel buildAndCache(Long provisionId) {
        ModelProvisionEntity provision = aiRepository.findModelProvisionById(provisionId);
        if (provision == null || !isActive(provision)) {
            throw new BizException(AiErrorCode.MODEL_PROVISION_DISABLED);
        }

        ModelEntity model = aiRepository.findModelById(provision.getModelId());
        if (model == null || !isEnabled(model.getStatus(), model.getDeleted())) {
            moderationModelRegistry.evict(provisionId);
            throw new BizException(AiErrorCode.MODEL_DISABLED);
        }

        ProviderEntity provider = aiRepository.findProviderById(model.getProviderId());
        if (provider == null || !isEnabled(provider.getStatus(), provider.getDeleted())) {
            moderationModelRegistry.evict(provisionId);
            throw new BizException(AiErrorCode.PROVIDER_DISABLED);
        }

        String apiKey = AesUtils.decrypt(provision.getApiKeyCipher(), cryptoProperties.getAesKey());
        if (apiKey == null || apiKey.isBlank()) {
            throw new BizException(AiErrorCode.API_KEY_MISSING);
        }

        ModerationModel moderationModel = moderationModelFactory.createModerationModel(provider, model, provision, apiKey);
        return moderationModelRegistry.put(provisionId, moderationModel);
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
