package cn.refinex.system.application.command;

import lombok.Data;

/**
 * 查询数据资源列表命令
 *
 * @author refinex
 */
@Data
public class QueryDrsListCommand {

    /**
     * 系统ID
     */
    private Long systemId;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 资源类型 0数据库表 1接口资源 2文件 3其他
     */
    private Integer drsType;

    /**
     * 所属组织ID(平台级为0)
     */
    private Long ownerEstabId;

    /**
     * 关键字（数据资源编码/名称）
     */
    private String keyword;
}
