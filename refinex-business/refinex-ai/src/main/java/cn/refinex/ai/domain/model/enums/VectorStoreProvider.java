package cn.refinex.ai.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 向量存储后端枚举
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum VectorStoreProvider {

    REDIS("redis", "Redis Stack"),
    ELASTICSEARCH("elasticsearch", "Elasticsearch"),
    PGVECTOR("pgvector", "PGvector"),
    QDRANT("qdrant", "Qdrant"),
    SIMPLE("simple", "SimpleVectorStore(内存/文件)");

    /**
     * 后端编码
     */
    private final String code;

    /**
     * 后端描述
     */
    private final String description;

    /**
     * 根据编码查找向量存储后端枚举
     *
     * @param code 后端编码
     * @return 向量存储后端枚举，未找到返回 null
     */
    public static VectorStoreProvider fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (VectorStoreProvider provider : values()) {
            if (provider.code.equalsIgnoreCase(code)) {
                return provider;
            }
        }
        return null;
    }
}
