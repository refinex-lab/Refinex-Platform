package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 更新值集明细命令
 *
 * @author refinex
 */
@Data
public class UpdateValueCommand {

    /**
     * 值ID
     */
    private Long valueId;

    /**
     * 值编码
     */
    private String valueCode;

    /**
     * 值名称
     */
    private String valueName;

    /**
     * 值描述
     */
    private String valueDesc;

    /**
     * 状态 1启用 0停用
     */
    private Integer status;

    /**
     * 是否默认 1是 0否
     */
    private Integer isDefault;

    /**
     * 排序
     */
    private Integer sort;
}
