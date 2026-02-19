package cn.refinex.system.interfaces.vo;

import lombok.Data;

/**
 * 值集 VO
 *
 * @author refinex
 */
@Data
public class ValueSetVO {

    /**
     * 值集ID
     */
    private Long id;

    /**
     * 值集编码
     */
    private String setCode;

    /**
     * 值集名称
     */
    private String setName;

    /**
     * 状态 1启用 0停用
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
