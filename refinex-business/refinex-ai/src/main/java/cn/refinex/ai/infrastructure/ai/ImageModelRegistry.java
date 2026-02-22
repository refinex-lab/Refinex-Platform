package cn.refinex.ai.infrastructure.ai;

import org.springframework.ai.image.ImageModel;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ImageModel 缓存注册表
 * <p>
 * 以 provisionId 为 key 缓存已创建的 ImageModel 实例。
 *
 * @author refinex
 */
@Component
public class ImageModelRegistry {

    private final ConcurrentHashMap<Long, ImageModel> cache = new ConcurrentHashMap<>();

    /**
     * 从缓存获取 ImageModel
     *
     * @param provisionId 租户模型开通ID
     * @return ImageModel 实例，未命中返回 null
     */
    public ImageModel get(Long provisionId) {
        return cache.get(provisionId);
    }

    /**
     * 写入缓存
     *
     * @param provisionId 租户模型开通ID
     * @param model       ImageModel 实例
     * @return 写入的 ImageModel 实例
     */
    public ImageModel put(Long provisionId, ImageModel model) {
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
