package cn.refinex.ai.infrastructure.ai;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * EmbeddingModel 缓存注册表
 * <p>
 * 以 provisionId 为 key 缓存已创建的 EmbeddingModel 实例。
 * EmbeddingModel 实例是线程安全的，并发读同一缓存条目无问题。
 *
 * @author refinex
 */
@Component
public class EmbeddingModelRegistry {

    private final ConcurrentHashMap<Long, EmbeddingModel> cache = new ConcurrentHashMap<>();

    /**
     * 从缓存获取 EmbeddingModel
     *
     * @param provisionId 租户模型开通ID
     * @return EmbeddingModel 实例，未命中返回 null
     */
    public EmbeddingModel get(Long provisionId) {
        return cache.get(provisionId);
    }

    /**
     * 写入缓存
     *
     * @param provisionId 租户模型开通ID
     * @param model       EmbeddingModel 实例
     * @return 写入的 Embeddin
     */
    public EmbeddingModel put(Long provisionId, EmbeddingModel model) {
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
