package cn.refinex.ai.interfaces.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新目录请求
 *
 * @author refinex
 */
@Data
public class FolderUpdateRequest {

    /**
     * 目录名称
     */
    @NotBlank(message = "目录名称不能为空")
    @Size(max = 128, message = "目录名称长度不能超过128个字符")
    private String folderName;

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
