package cn.refinex.system.interfaces.vo;

import lombok.Data;

/**
 * 操作定义 VO
 *
 * @author refinex
 */
@Data
public class OpVO {

    /**
     * 操作ID
     */
    private Long id;

    /**
     * 操作编码
     */
    private String opCode;

    /**
     * 操作名称
     */
    private String opName;

    /**
     * 操作说明
     */
    private String opDesc;

    /**
     * 是否内置 1是 0否
     */
    private Integer isBuiltin;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;
}
