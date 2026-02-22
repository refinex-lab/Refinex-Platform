package cn.refinex.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Refinex AI Application
 *
 * @author refinex
 */
@EnableDiscoveryClient
@MapperScan("cn.refinex.ai.infrastructure.persistence.mapper")
@SpringBootApplication(
        scanBasePackages = "cn.refinex",
        exclude = {
                org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration.class,
                org.springframework.ai.model.openai.autoconfigure.OpenAiImageAutoConfiguration.class,
                org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechAutoConfiguration.class,
                org.springframework.ai.model.openai.autoconfigure.OpenAiAudioTranscriptionAutoConfiguration.class,
                org.springframework.ai.model.openai.autoconfigure.OpenAiModerationAutoConfiguration.class,
                org.springframework.ai.model.deepseek.autoconfigure.DeepSeekChatAutoConfiguration.class,
                org.springframework.ai.model.anthropic.autoconfigure.AnthropicChatAutoConfiguration.class,
                org.springframework.ai.model.minimax.autoconfigure.MiniMaxChatAutoConfiguration.class,
                org.springframework.ai.model.zhipuai.autoconfigure.ZhiPuAiChatAutoConfiguration.class,
                org.springframework.ai.model.zhipuai.autoconfigure.ZhiPuAiImageAutoConfiguration.class,
                org.springframework.ai.model.chat.client.autoconfigure.ChatClientAutoConfiguration.class,
                // Embedding 自动配置排除（动态路由，不用单例 bean）
                org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration.class,
                org.springframework.ai.model.zhipuai.autoconfigure.ZhiPuAiEmbeddingAutoConfiguration.class,
                org.springframework.ai.model.minimax.autoconfigure.MiniMaxEmbeddingAutoConfiguration.class,
                // VectorStore 自动配置排除（程序化创建，不用单例 bean）
                org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreAutoConfiguration.class,
                org.springframework.ai.vectorstore.elasticsearch.autoconfigure.ElasticsearchVectorStoreAutoConfiguration.class,
                org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration.class,
                org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreAutoConfiguration.class,
                // JdbcChatMemoryRepository 自动配置排除（引用了 Spring Boot 3.x 的 JdbcTemplateAutoConfiguration，与 Boot 4 不兼容，手动配置替代）
                org.springframework.ai.model.chat.memory.repository.jdbc.autoconfigure.JdbcChatMemoryRepositoryAutoConfiguration.class,
        }
)
public class RefinexAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RefinexAiApplication.class, args);
    }
}
