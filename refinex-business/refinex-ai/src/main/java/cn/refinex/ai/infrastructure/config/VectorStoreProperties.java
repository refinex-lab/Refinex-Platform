package cn.refinex.ai.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 向量存储配置属性
 * <p>
 * 系统级配置，所有知识库共用同一后端。通过 Document metadata 实现多租户/多知识库隔离。
 *
 * @author refinex
 */
@Data
@ConfigurationProperties(prefix = "refinex.vector-store")
public class VectorStoreProperties {

    /**
     * 激活的向量存储后端 (redis/elasticsearch/pgvector/qdrant/simple)
     */
    private String provider = "simple";

    /**
     * 是否自动初始化 schema
     */
    private boolean initializeSchema = true;

    /**
     * Redis 向量存储配置
     */
    private RedisProperties redis = new RedisProperties();

    /**
     * Elasticsearch 向量存储配置
     */
    private ElasticsearchProperties elasticsearch = new ElasticsearchProperties();

    /**
     * PGvector 向量存储配置
     */
    private PgVectorProperties pgvector = new PgVectorProperties();

    /**
     * Qdrant 向量存储配置
     */
    private QdrantProperties qdrant = new QdrantProperties();

    /**
     * SimpleVectorStore 配置
     */
    private SimpleProperties simple = new SimpleProperties();

    /**
     * Redis 向量存储配置
     */
    @Data
    public static class RedisProperties {

        /**
         * Redis 索引名称
         */
        private String indexName = "refinex-vectors";

        /**
         * Redis key 前缀
         */
        private String prefix = "refinex:vec:";
    }

    /**
     * Elasticsearch 向量存储配置
     */
    @Data
    public static class ElasticsearchProperties {

        /**
         * 索引名称
         */
        private String indexName = "refinex-vectors";

        /**
         * 向量维度
         */
        private int dimensions = 1536;

        /**
         * 相似度算法
         */
        private String similarity = "cosine";
    }

    /**
     * PGvector 向量存储配置
     */
    @Data
    public static class PgVectorProperties {

        /**
         * 向量表名
         */
        private String tableName = "vector_store";

        /**
         * Schema 名称
         */
        private String schemaName = "public";

        /**
         * 向量维度
         */
        private int dimensions = 1536;
    }

    /**
     * Qdrant 向量存储配置
     */
    @Data
    public static class QdrantProperties {

        /**
         * Qdrant 主机地址
         */
        private String host = "localhost";

        /**
         * Qdrant gRPC 端口
         */
        private int port = 6334;

        /**
         * Qdrant API Key
         */
        private String apiKey;

        /**
         * 集合名称
         */
        private String collectionName = "refinex-vectors";

        /**
         * 是否启用 TLS
         */
        private boolean useTls = false;
    }

    /**
     * SimpleVectorStore 配置
     */
    @Data
    public static class SimpleProperties {

        /**
         * JSON 持久化文件路径
         */
        private String filePath = "/tmp/refinex-vectors.json";
    }
}
