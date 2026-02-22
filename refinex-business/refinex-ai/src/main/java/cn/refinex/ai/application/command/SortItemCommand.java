package cn.refinex.ai.application.command;

import lombok.Data;

/**
 * 排序项命令
 *
 * @author refinex
 */
@Data
public class SortItemCommand {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 类型 FOLDER/DOCUMENT
     */
    private String type;

    /**
     * 排序值
     */
    private Integer sort;
}
