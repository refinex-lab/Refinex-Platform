package cn.refinex.ai.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 知识库文档 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_document")
public class KbDocumentDo extends BaseEntity {

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
     * 提取后的纯文本内容
     */
    private String content;

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
