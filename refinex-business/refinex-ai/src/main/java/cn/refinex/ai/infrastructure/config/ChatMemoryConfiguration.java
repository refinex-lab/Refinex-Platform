package cn.refinex.ai.infrastructure.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * ChatMemory 配置
 *
 * @author refinex
 */
@Configuration
public class ChatMemoryConfiguration {

    /**
     * 手动创建 JdbcChatMemoryRepository（替代 Spring AI 自动配置，因其引用了 Boot 3.x 已移除的 JdbcTemplateAutoConfiguration）
     *
     * @param jdbcTemplate JdbcTemplate（由 Spring Boot 自动配置）
     * @return JdbcChatMemoryRepository 实例
     */
    @Bean
    public JdbcChatMemoryRepository jdbcChatMemoryRepository(JdbcTemplate jdbcTemplate) {
        return JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .build();
    }

    /**
     * 基于 JDBC 的 ChatMemory（滑动窗口策略，最多保留 20 条消息）
     *
     * @param jdbcChatMemoryRepository JDBC 聊天记忆仓储
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
