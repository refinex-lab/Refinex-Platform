package cn.refinex.ai.application.command;

import lombok.Data;

/**
 * 更新知识库命令
 *
 * @author refinex
 */
@Data
public class UpdateKnowledgeBaseCommand {

    /**
     * 主键ID
     */
    private Long kbId;

    /**
     * 知识库名称
     */
    private String kbName;

    /**
     * 知识库描述
     */
    private String description;

    /**
     * 图标
     */
    private String icon;

    /**
     * 可见性 0私有 1组织内公开 2平台公开
     */
    private Integer visibility;

    /**
     * 是否开启向量化 1是 0否
     */
    private Integer vectorized;

    /**
     * 嵌入模型ID(开启向量化时必填)
     */
    private Long embeddingModelId;

    /**
     * 文档切片大小(token数)
     */
    private Integer chunkSize;

    /**
     * 切片重叠大小(token数)
     */
    private Integer chunkOverlap;

    /**
     * 状态 1启用 0停用
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
     * 扩展信息
     */
    private String extJson;
}
