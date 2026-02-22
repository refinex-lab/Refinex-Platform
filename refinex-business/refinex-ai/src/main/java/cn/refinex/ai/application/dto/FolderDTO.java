package cn.refinex.ai.application.dto;

import lombok.Data;

/**
 * 目录 DTO
 *
 * @author refinex
 */
@Data
public class FolderDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 父目录ID(0为根级)
     */
    private Long parentId;

    /**
     * 目录名称
     */
    private String folderName;

    /**
     * 排序(升序)
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展信息
     */
    private String extJson;
}
