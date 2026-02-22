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
                org.springframework.ai.model.deepseek.autoconfigure.DeepSeekChatAutoConfiguration.class,
                org.springframework.ai.model.anthropic.autoconfigure.AnthropicChatAutoConfiguration.class,
                org.springframework.ai.model.minimax.autoconfigure.MiniMaxChatAutoConfiguration.class,
                org.springframework.ai.model.zhipuai.autoconfigure.ZhiPuAiChatAutoConfiguration.class,
                org.springframework.ai.model.zhipuai.autoconfigure.ZhiPuAiImageAutoConfiguration.class,
                org.springframework.ai.model.chat.client.autoconfigure.ChatClientAutoConfiguration.class,
        }
)
public class RefinexAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RefinexAiApplication.class, args);
    }
}
