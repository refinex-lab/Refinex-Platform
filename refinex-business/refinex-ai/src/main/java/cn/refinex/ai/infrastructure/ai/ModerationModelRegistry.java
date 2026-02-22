package cn.refinex.ai.infrastructure.ai;

import org.springframework.ai.moderation.ModerationModel;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ModerationModel 缓存注册表
 * <p>
 * 以 provisionId 为 key 缓存已创建的 ModerationModel 实例。
 *
 * @author refinex
 */
@Component
public class ModerationModelRegistry {

    private final ConcurrentHashMap<Long, ModerationModel> cache = new ConcurrentHashMap<>();

    /**
     * 从缓存获取 ModerationModel
     *
     * @param provisionId 租户模型开通ID
     * @return ModerationModel 实例，未命中返回 null
     */
    public ModerationModel get(Long provisionId) {
        return cache.get(provisionId);
    }

    /**
     * 写入缓存
     *
     * @param provisionId 租户模型开通ID
     * @param model       ModerationModel 实例
     * @return 写入的 ModerationModel 实例
     */
    public ModerationModel put(Long provisionId, ModerationModel model) {
        cache.put(provisionId, model);
        return model;
    }

    /**
     * 驱逐单条缓存
     *
     * @param provisionId 租户模型开通ID
     */
    public void evict(Long provisionId) {
        cache.remove(provisionId);
    }

    /**
     * 清空全部缓存
     */
    public void evictAll() {
        cache.clear();
    }
}
