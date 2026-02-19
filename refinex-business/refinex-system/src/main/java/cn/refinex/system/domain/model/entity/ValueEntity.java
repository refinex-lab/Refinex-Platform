package cn.refinex.system.domain.model.entity;

import lombok.Data;

/**
 * 值集明细实体
 *
 * @author refinex
 */
@Data
public class ValueEntity {

    /**
     * 值ID
     */
    private Long id;

    /**
     * 值集编码
     */
    private String setCode;

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

    /**
     * 是否删除 1是 0否
     */
    private Integer deleted;
}
