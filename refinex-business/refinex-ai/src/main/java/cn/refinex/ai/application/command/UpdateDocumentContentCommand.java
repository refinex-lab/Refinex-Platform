package cn.refinex.ai.application.command;

import lombok.Data;

/**
 * 更新文档内容命令
 *
 * @author refinex
 */
@Data
public class UpdateDocumentContentCommand {

    /**
     * 主键ID
     */
    private Long documentId;

    /**
     * 文档纯文本内容
     */
    private String content;
}
