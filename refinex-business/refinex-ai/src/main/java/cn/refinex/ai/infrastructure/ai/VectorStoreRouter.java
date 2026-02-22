package cn.refinex.ai.infrastructure.ai;

import cn.refinex.ai.domain.error.AiErrorCode;
import cn.refinex.ai.domain.model.entity.KnowledgeBaseEntity;
import cn.refinex.ai.domain.model.enums.VectorStoreProvider;
import cn.refinex.ai.infrastructure.config.VectorStoreProperties;
import cn.refinex.base.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * VectorStore 路由器
 * <p>
 * 按嵌入模型 provisionId 缓存 VectorStore 实例。
 * 同一嵌入模型的多个知识库共享同一 VectorStore 实例，通过 metadata 隔离数据。
 *
 * @author refinex
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VectorStoreRouter {

    private final VectorStoreProperties vectorStoreProperties;
    private final VectorStoreFactory vectorStoreFactory;
    private final EmbeddingModelRouter embeddingModelRouter;
    private final ConcurrentHashMap<Long, VectorStore> cache = new ConcurrentHashMap<>();

    /**
     * 为知识库解析 VectorStore
     *
     * @param kb 知识库实体
     * @return VectorStore 实例
     */
    public VectorStore resolve(KnowledgeBaseEntity kb) {
        if (kb.getVectorized() == null || kb.getVectorized() != 1) {
            throw new BizException(AiErrorCode.KB_NOT_VECTORIZED);
        }

        VectorStoreProvider provider = VectorStoreProvider.fromCode(vectorStoreProperties.getProvider());
        if (provider == null) {
            throw new BizException(AiErrorCode.VECTOR_STORE_NOT_CONFIGURED);
        }

        Long embeddingProvisionId = embeddingModelRouter.resolveProvisionIdForKnowledgeBase(kb);

        return cache.computeIfAbsent(embeddingProvisionId, key -> {
            EmbeddingModel embeddingModel = embeddingModelRouter.resolve(key);
            VectorStore store = vectorStoreFactory.create(provider, embeddingModel);
            log.info("创建 VectorStore 缓存: provisionId={}, provider={}", key, provider.getCode());
            return store;
        });
    }

    /**
     * 驱逐指定嵌入模型的 VectorStore 缓存
     *
     * @param embeddingProvisionId 嵌入模型开通ID
     */
    public void evict(Long embeddingProvisionId) {
        cache.remove(embeddingProvisionId);
    }

    /**
     * 清空全部 VectorStore 缓存
     */
    public void evictAll() {
        cache.clear();
    }
}
