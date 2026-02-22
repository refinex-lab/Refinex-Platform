package cn.refinex.ai.infrastructure.ai;

import cn.refinex.ai.domain.model.enums.VectorStoreProvider;
import cn.refinex.ai.infrastructure.config.VectorStoreProperties;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPooled;

import java.io.File;

/**
 * VectorStore 工厂
 * <p>
 * 根据系统配置的向量存储后端，程序化创建 VectorStore 实例。
 * 每个 VectorStore 实例绑定一个 EmbeddingModel（VectorStore.add() 内部调用 embed()）。
 *
 * @author refinex
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VectorStoreFactory {

    private final VectorStoreProperties properties;
    private final ObjectProvider<JedisPooled> jedisPooledProvider;
    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectProvider<RestClient> esRestClientProvider;

    /**
     * 根据后端类型创建 VectorStore 实例
     *
     * @param provider       向量存储后端类型
     * @param embeddingModel 嵌入模型
     * @return VectorStore 实例
     */
    public VectorStore create(VectorStoreProvider provider, EmbeddingModel embeddingModel) {
        return switch (provider) {
            case REDIS -> createRedis(embeddingModel);
            case ELASTICSEARCH -> createElasticsearch(embeddingModel);
            case PGVECTOR -> createPgVector(embeddingModel);
            case QDRANT -> createQdrant(embeddingModel);
            case SIMPLE -> createSimple(embeddingModel);
        };
    }

    /**
     * 创建 Redis VectorStore
     *
     * @param embeddingModel 嵌入模型
     * @return RedisVectorStore 实例
     */
    private VectorStore createRedis(EmbeddingModel embeddingModel) {
        JedisPooled jedis = jedisPooledProvider.getIfAvailable();
        if (jedis == null) {
            throw new IllegalStateException("Redis VectorStore 需要 JedisPooled bean，请检查 Redis 配置");
        }

        VectorStoreProperties.RedisProperties redisCfg = properties.getRedis();

        RedisVectorStore store = RedisVectorStore.builder(jedis, embeddingModel)
                .indexName(redisCfg.getIndexName())
                .prefix(redisCfg.getPrefix())
                .initializeSchema(properties.isInitializeSchema())
                .build();

        log.info("创建 Redis VectorStore: indexName={}, prefix={}", redisCfg.getIndexName(), redisCfg.getPrefix());
        return store;
    }

    /**
     * 创建 Elasticsearch VectorStore
     *
     * @param embeddingModel 嵌入模型
     * @return ElasticsearchVectorStore 实例
     */
    private VectorStore createElasticsearch(EmbeddingModel embeddingModel) {
        RestClient restClient = esRestClientProvider.getIfAvailable();
        if (restClient == null) {
            throw new IllegalStateException("Elasticsearch VectorStore 需要 RestClient bean，请检查 Elasticsearch 配置");
        }

        VectorStoreProperties.ElasticsearchProperties esCfg = properties.getElasticsearch();

        ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
        options.setIndexName(esCfg.getIndexName());
        options.setDimensions(esCfg.getDimensions());

        ElasticsearchVectorStore store = ElasticsearchVectorStore.builder(restClient, embeddingModel)
                .options(options)
                .initializeSchema(properties.isInitializeSchema())
                .build();

        log.info("创建 Elasticsearch VectorStore: indexName={}, dimensions={}", esCfg.getIndexName(), esCfg.getDimensions());
        return store;
    }

    /**
     * 创建 PGvector VectorStore
     *
     * @param embeddingModel 嵌入模型
     * @return PgVectorStore 实例
     */
    private VectorStore createPgVector(EmbeddingModel embeddingModel) {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new IllegalStateException("PGvector VectorStore 需要 JdbcTemplate bean，请检查数据源配置");
        }

        VectorStoreProperties.PgVectorProperties pgCfg = properties.getPgvector();

        PgVectorStore store = PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(pgCfg.getDimensions())
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .initializeSchema(properties.isInitializeSchema())
                .schemaName(pgCfg.getSchemaName())
                .vectorTableName(pgCfg.getTableName())
                .build();

        log.info("创建 PGvector VectorStore: schema={}, table={}, dimensions={}",
                pgCfg.getSchemaName(), pgCfg.getTableName(), pgCfg.getDimensions());
        return store;
    }

    /**
     * 创建 Qdrant VectorStore
     *
     * @param embeddingModel 嵌入模型
     * @return QdrantVectorStore 实例
     */
    private VectorStore createQdrant(EmbeddingModel embeddingModel) {
        VectorStoreProperties.QdrantProperties qdrantCfg = properties.getQdrant();

        QdrantGrpcClient.Builder grpcBuilder = QdrantGrpcClient.newBuilder(qdrantCfg.getHost(), qdrantCfg.getPort(), qdrantCfg.isUseTls());
        if (qdrantCfg.getApiKey() != null && !qdrantCfg.getApiKey().isBlank()) {
            grpcBuilder.withApiKey(qdrantCfg.getApiKey());
        }
        QdrantClient qdrantClient = new QdrantClient(grpcBuilder.build());

        QdrantVectorStore store = QdrantVectorStore.builder(qdrantClient, embeddingModel)
                .collectionName(qdrantCfg.getCollectionName())
                .initializeSchema(properties.isInitializeSchema())
                .build();

        log.info("创建 Qdrant VectorStore: host={}:{}, collection={}",
                qdrantCfg.getHost(), qdrantCfg.getPort(), qdrantCfg.getCollectionName());
        return store;
    }

    /**
     * 创建 SimpleVectorStore（内存/文件，开发测试用）
     *
     * @param embeddingModel 嵌入模型
     * @return SimpleVectorStore 实例
     */
    private VectorStore createSimple(EmbeddingModel embeddingModel) {
        VectorStoreProperties.SimpleProperties simpleCfg = properties.getSimple();

        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();

        // 如果持久化文件存在，加载已有向量
        File file = new File(simpleCfg.getFilePath());
        if (file.exists()) {
            store.load(file);
            log.info("SimpleVectorStore 从文件加载向量: {}", simpleCfg.getFilePath());
        }

        log.info("创建 SimpleVectorStore: filePath={}", simpleCfg.getFilePath());
        return store;
    }
}
