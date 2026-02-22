package cn.refinex.ai.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文字转语音请求
 *
 * @author refinex
 */
@Data
public class TtsRequest {

    /**
     * 文本内容
     */
    @NotBlank(message = "文本内容不能为空")
    @Size(max = 4096, message = "文本长度不能超过4096字符")
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
}
