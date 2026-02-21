package cn.refinex.ai.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新Prompt模板请求
 *
 * @author refinex
 */
@Data
public class PromptTemplateUpdateRequest {

    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 128, message = "模板名称长度不能超过128个字符")
    private String promptName;

    /**
     * 分类(如 rag/summary/translate/code_gen/chat)
     */
    @Size(max = 32, message = "分类长度不能超过32个字符")
    private String category;

    /**
     * 模板内容(变量占位符格式: 开始界定符+变量名+结束界定符)
     */
    private String content;

    /**
     * 变量定义([{"name":"context","desc":"上下文","required":true}])
     */
    private String variables;

    /**
     * 变量占位符开始界定符(默认 {{)
     */
    @Size(max = 8, message = "变量开始界定符长度不能超过8个字符")
    private String varOpen;

    /**
     * 变量占位符结束界定符(默认 }})
     */
    @Size(max = 8, message = "变量结束界定符长度不能超过8个字符")
    private String varClose;

    /**
     * 语言
     */
    @Size(max = 16, message = "语言长度不能超过16个字符")
    private String language;

    /**
     * 是否内置 1是 0否
     */
    @Min(value = 0, message = "是否内置取值非法")
    @Max(value = 1, message = "是否内置取值非法")
    private Integer isBuiltin;

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
