package cn.refinex.ai.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新文档内容请求
 *
 * @author refinex
 */
@Data
public class DocumentContentUpdateRequest {

    /**
     * 文档纯文本内容
     */
    @NotNull(message = "文档内容不能为null")
    private String content;
}
