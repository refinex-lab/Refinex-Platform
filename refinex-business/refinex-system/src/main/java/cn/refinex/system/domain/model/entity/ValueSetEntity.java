package cn.refinex.system.domain.model.entity;

import lombok.Data;

/**
 * 值集实体
 *
 * @author refinex
 */
@Data
public class ValueSetEntity {

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

    /**
     * 是否删除 1是 0否
     */
    private Integer deleted;
}
