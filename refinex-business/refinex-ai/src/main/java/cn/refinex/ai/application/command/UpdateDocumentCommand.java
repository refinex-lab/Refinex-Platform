package cn.refinex.ai.application.command;

import lombok.Data;

/**
 * 更新文档命令
 *
 * @author refinex
 */
@Data
public class UpdateDocumentCommand {

    /**
     * 主键ID
     */
    private Long documentId;

    /**
     * 文档名称
     */
    private String docName;

    /**
     * 目录ID(0为根目录)
     */
    private Long folderId;

    /**
     * 状态 1正常 0禁用
     */
    private Integer status;

    /**
     * 排序(升序)
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展信息(如文档元数据：作者、页数等)
     */
    private String extJson;
}
