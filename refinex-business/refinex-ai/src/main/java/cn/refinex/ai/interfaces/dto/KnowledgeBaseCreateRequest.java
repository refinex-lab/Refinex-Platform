package cn.refinex.ai.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建知识库请求
 *
 * @author refinex
 */
@Data
public class KnowledgeBaseCreateRequest {

    /**
     * 知识库编码
     */
    @NotBlank(message = "知识库编码不能为空")
    @Size(max = 64, message = "知识库编码长度不能超过64个字符")
    private String kbCode;

    /**
     * 知识库名称
     */
    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 128, message = "知识库名称长度不能超过128个字符")
    private String kbName;

    /**
     * 知识库描述
     */
    @Size(max = 1024, message = "知识库描述长度不能超过1024个字符")
    private String description;

    /**
     * 图标
     */
    @Size(max = 255, message = "图标地址长度不能超过255个字符")
    private String icon;

    /**
     * 可见性 0私有 1组织内公开 2平台公开
     */
    @Min(value = 0, message = "可见性取值非法")
    @Max(value = 2, message = "可见性取值非法")
    private Integer visibility;

    /**
     * 是否开启向量化 1是 0否
     */
    @Min(value = 0, message = "是否开启向量化取值非法")
    @Max(value = 1, message = "是否开启向量化取值非法")
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
    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status;

    /**
     * 排序(升序)
     */
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;

    /**
     * 扩展信息
     */
    private String extJson;
}
