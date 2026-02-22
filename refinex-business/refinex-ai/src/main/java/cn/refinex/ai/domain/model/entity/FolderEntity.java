package cn.refinex.ai.domain.model.entity;

import lombok.Data;

/**
 * 知识库目录领域实体
 *
 * @author refinex
 */
@Data
public class FolderEntity {

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

    /**
     * 逻辑删除 0未删 1已删
     */
    private Integer deleted;
}
