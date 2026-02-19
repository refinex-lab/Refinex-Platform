package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 创建值集命令
 *
 * @author refinex
 */
@Data
public class CreateValueSetCommand {

    /**
     * 值集编码
     */
    private String setCode;

    /**
     * 值集名称
     */
    private String setName;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 描述
     */
    private String description;
}
