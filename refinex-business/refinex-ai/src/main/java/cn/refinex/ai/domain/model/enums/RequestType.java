package cn.refinex.ai.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 请求类型
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum RequestType {

    CHAT("CHAT", "聊天"),
    EMBEDDING("EMBEDDING", "嵌入"),
    IMAGE_GEN("IMAGE_GEN", "图像生成"),
    TTS("TTS", "文字转语音"),
    STT("STT", "语音转文字"),
    RERANK("RERANK", "重排序"),
    ;

    /**
     * 类型编码
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String description;
}
