package cn.refinex.system.domain.model.entity;

import lombok.Data;

/**
 * 数据资源实体
 *
 * @author refinex
 */
@Data
public class DrsEntity {

    /**
     * 数据资源ID
     */
    private Long id;

    /**
     * 数据资源编码
     */
    private String drsCode;

    /**
     * 数据资源名称
     */
    private String drsName;

    /**
     * 所属组织ID(平台级为0)
     */
    private Long ownerEstabId;

    /**
     * 数据归属 0平台 1租户
     */
    private Integer dataOwnerType;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否删除 1是 0否
     */
    private Integer deleted;
}
