package cn.refinex.ai.application.dto;

import lombok.Data;

/**
 * Prompt模板 DTO
 *
 * @author refinex
 */
@Data
public class PromptTemplateDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 组织ID(平台级为0)
     */
    private Long estabId;

    /**
     * 模板编码
     */
    private String promptCode;

    /**
     * 模板名称
     */
    private String promptName;

    /**
     * 分类(如 rag/summary/translate/code_gen/chat)
     */
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
    private String varOpen;

    /**
     * 变量占位符结束界定符(默认 }})
     */
    private String varClose;

    /**
     * 语言
     */
    private String language;

    /**
     * 是否内置 1是 0否
     */
    private Integer isBuiltin;

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
