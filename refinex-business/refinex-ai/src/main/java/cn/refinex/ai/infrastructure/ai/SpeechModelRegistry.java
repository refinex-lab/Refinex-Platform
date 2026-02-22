package cn.refinex.ai.infrastructure.ai;

import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * TextToSpeechModel 缓存注册表
 * <p>
 * 以 provisionId 为 key 缓存已创建的 TextToSpeechModel 实例。
 *
 * @author refinex
 */
@Component
public class SpeechModelRegistry {

    private final ConcurrentHashMap<Long, TextToSpeechModel> cache = new ConcurrentHashMap<>();

    /**
     * 从缓存获取 TextToSpeechModel
     *
     * @param provisionId 租户模型开通ID
     * @return TextToSpeechModel 实例，未命中返回 null
     */
    public TextToSpeechModel get(Long provisionId) {
        return cache.get(provisionId);
    }

    /**
     * 写入缓存
     *
     * @param provisionId 租户模型开通ID
     * @param model       TextToSpeechModel 实例
     * @return 写入的 TextToSpeechModel 实例
     */
    public TextToSpeechModel put(Long provisionId, TextToSpeechModel model) {
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
