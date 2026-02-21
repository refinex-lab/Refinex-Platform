package cn.refinex.ai.application.command;

import lombok.Data;

/**
 * 更新Prompt模板命令
 *
 * @author refinex
 */
@Data
public class UpdatePromptTemplateCommand {

    /**
     * 主键ID
     */
    private Long promptTemplateId;

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
