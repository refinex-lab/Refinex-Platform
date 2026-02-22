package cn.refinex.ai.application.command;

import lombok.Data;

/**
 * 文字转语音命令
 *
 * @author refinex
 */
@Data
public class TtsCommand {

    /**
     * 文本内容
     */
    private String text;

    /**
     * TTS模型ID(可选，为空用租户默认TTS模型)
     */
    private Long modelId;

    /**
     * 语音风格(alloy/echo/fable/onyx/nova/shimmer等，默认alloy)
     */
    private String voice;

    /**
     * 语速(0.25~4.0，默认1.0)
     */
    private Double speed;

    /**
     * 组织ID(Controller 从 LoginUserHelper 注入)
     */
    private Long estabId;

    /**
     * 用户ID(Controller 从 LoginUserHelper 注入)
     */
    private Long userId;
}
