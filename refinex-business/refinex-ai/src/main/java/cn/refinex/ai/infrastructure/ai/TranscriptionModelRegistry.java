package cn.refinex.ai.infrastructure.ai;

import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * TranscriptionModel 缓存注册表
 * <p>
 * 以 provisionId 为 key 缓存已创建的 TranscriptionModel 实例。
 *
 * @author refinex
 */
@Component
public class TranscriptionModelRegistry {

    private final ConcurrentHashMap<Long, TranscriptionModel> cache = new ConcurrentHashMap<>();

    /**
     * 从缓存获取 TranscriptionModel
     *
     * @param provisionId 租户模型开通ID
     * @return TranscriptionModel 实例，未命中返回 null
     */
    public TranscriptionModel get(Long provisionId) {
        return cache.get(provisionId);
    }

    /**
     * 写入缓存
     *
     * @param provisionId 租户模型开通ID
     * @param model       TranscriptionModel 实例
     * @return 写入的 TranscriptionModel 实例
     */
    public TranscriptionModel put(Long provisionId, TranscriptionModel model) {
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
