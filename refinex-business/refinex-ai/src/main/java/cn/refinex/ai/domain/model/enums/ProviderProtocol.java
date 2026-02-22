package cn.refinex.ai.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 供应商接口协议
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum ProviderProtocol {

    OPENAI("openai", "OpenAI 协议"),
    ANTHROPIC("anthropic", "Anthropic 协议"),
    OLLAMA("ollama", "Ollama 协议"),
    ;

    /**
     * 协议编码
     */
    private final String code;

    /**
     * 协议描述
     */
    private final String description;

    /**
     * 根据编码查找协议枚举
     *
     * @param code 协议编码
     * @return 协议枚举，未找到返回 null
     */
    public static ProviderProtocol fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ProviderProtocol protocol : values()) {
            if (protocol.code.equals(code)) {
                return protocol;
            }
        }
        return null;
    }
}
