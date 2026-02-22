package cn.refinex.ai.application.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 文档 DTO
 *
 * @author refinex
 */
@Data
public class DocumentDTO {

    /**
     * 主键ID
     */
    private Long id;

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
     * 字符数
     */
    private Integer charCount;

    /**
     * 估算token数
     */
    private Integer tokenCount;

    /**
     * 向量化状态 0未向量化 1向量化中 2已完成 3失败
     */
    private Integer vectorStatus;

    /**
     * 向量化失败原因
     */
    private String vectorError;

    /**
     * 切片数量
     */
    private Integer chunkCount;

    /**
     * 最近向量化完成时间
     */
    private LocalDateTime lastVectorizedAt;

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
