package cn.refinex.ai.application.command;

import lombok.Data;

/**
 * 创建文档命令
 *
 * @author refinex
 */
@Data
public class CreateDocumentCommand {

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 目录ID(0为根目录)
     */
    private Long folderId;

    /**
     * 文档名称
     */
    private String docName;

    /**
     * 文档类型(MD/PDF/DOCX/XLSX/PPTX/TXT/HTML/CSV)
     */
    private String docType;

    /**
     * 原始文件存储地址
     */
    private String fileUrl;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

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
