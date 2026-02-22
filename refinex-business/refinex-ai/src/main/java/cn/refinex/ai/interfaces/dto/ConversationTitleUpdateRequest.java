package cn.refinex.ai.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 对话标题更新请求
 *
 * @author refinex
 */
@Data
public class ConversationTitleUpdateRequest {

    /**
     * 对话标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255")
    private String title;
}
