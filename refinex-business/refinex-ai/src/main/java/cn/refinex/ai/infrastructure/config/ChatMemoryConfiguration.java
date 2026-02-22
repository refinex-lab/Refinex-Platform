package cn.refinex.ai.infrastructure.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatMemory 配置
 *
 * @author refinex
 */
@Configuration
public class ChatMemoryConfiguration {

    /**
     * 基于 JDBC 的 ChatMemory（滑动窗口策略，最多保留 20 条消息）
     *
     * @param jdbcChatMemoryRepository JDBC 聊天记忆仓储（由 spring-ai-starter 自动配置）
     * @return ChatMemory 实例
     */
    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(20)
                .build();
    }
}
