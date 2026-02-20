package cn.refinex.system.domain.model.entity;

import lombok.Data;

/**
 * 数据资源接口实体
 *
 * @author refinex
 */
@Data
public class DrsInterfaceEntity {

    /**
     * 接口ID
     */
    private Long id;

    /**
     * 数据资源ID
     */
    private Long drsId;

    /**
     * 接口编码
     */
    private String interfaceCode;

    /**
     * 接口名称
     */
    private String interfaceName;

    /**
     * 数据资源 SQL 过滤表达式
     */
    private String interfaceSql;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否删除 1是 0否
     */
    private Integer deleted;
}
