package cn.refinex.system.interfaces.vo;

import lombok.Data;

/**
 * 数据资源接口 VO
 *
 * @author refinex
 */
@Data
public class DrsInterfaceVO {

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
}
