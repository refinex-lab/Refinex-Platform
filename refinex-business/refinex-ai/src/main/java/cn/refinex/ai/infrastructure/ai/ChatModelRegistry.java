package cn.refinex.ai.infrastructure.ai;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatModel 缓存注册表
 * <p>
 * 以 provisionId 为 key 缓存已创建的 ChatModel 实例。
 * ChatModel 实例是线程安全的，并发读同一缓存条目无问题。
 *
 * @author refinex
 */
@Component
public class ChatModelRegistry {

    private final ConcurrentHashMap<Long, ChatModel> cache = new ConcurrentHashMap<>();

    /**
     * 从缓存获取 ChatModel
     *
     * @param provisionId 租户模型开通ID
     * @return ChatModel 实例，未命中返回 null
     */
    public ChatModel get(Long provisionId) {
        return cache.get(provisionId);
    }

    /**
     * 写入缓存
     *
     * @param provisionId 租户模型开通ID
     * @param model       ChatModel 实例
     * @return 写入的 ChatModel 实例
     */
    public ChatModel put(Long provisionId, ChatModel model) {
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
