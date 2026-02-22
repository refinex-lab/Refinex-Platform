package cn.refinex.ai.domain.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 模型类型
 *
 * @author refinex
 */
@Getter
@AllArgsConstructor
public enum ModelType {

    CHAT(1, "聊天"),
    EMBEDDING(2, "嵌入"),
    IMAGE_GEN(3, "图像生成"),
    STT(4, "语音转文字"),
    TTS(5, "文字转语音"),
    RERANK(6, "重排序"),
    ;

    /**
     * 模型类型 1聊天 2嵌入 3图像生成 4语音转文字 5文字转语音 6重排序
     */
    private final int code;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 判断给定类型值是否为图像生成
     *
     * @param type 模型类型值（可为 null）
     * @return true 表示图像生成类型
     */
    public static boolean isImageGen(Integer type) {
        return type != null && type == IMAGE_GEN.code;
    }

    /**
     * 判断给定类型值是否为文字转语音
     *
     * @param type 模型类型值（可为 null）
     * @return true 表示文字转语音类型
     */
    public static boolean isTts(Integer type) {
        return type != null && type == TTS.code;
    }

    /**
     * 判断给定类型值是否为语音转文字
     *
     * @param type 模型类型值（可为 null）
     * @return true 表示语音转文字类型
     */
    public static boolean isStt(Integer type) {
        return type != null && type == STT.code;
    }

    /**
     * 判断给定类型值是否需要 ChatModel（非图像生成/TTS/STT 等）
     *
     * @param type 模型类型值（可为 null）
     * @return true 表示需要 ChatModel
     */
    public static boolean requiresChatModel(Integer type) {
        return type == null || type == CHAT.code;
    }
}
