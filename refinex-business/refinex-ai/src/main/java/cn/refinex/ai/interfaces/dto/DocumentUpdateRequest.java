package cn.refinex.ai.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新文档请求
 *
 * @author refinex
 */
@Data
public class DocumentUpdateRequest {

    /**
     * 文档名称
     */
    @NotBlank(message = "文档名称不能为空")
    @Size(max = 255, message = "文档名称长度不能超过255个字符")
    private String docName;

    /**
     * 目录ID(0为根目录)
     */
    private Long folderId;

    /**
     * 状态 1正常 0禁用
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
     * 扩展信息(如文档元数据：作者、页数等)
     */
    private String extJson;
}
