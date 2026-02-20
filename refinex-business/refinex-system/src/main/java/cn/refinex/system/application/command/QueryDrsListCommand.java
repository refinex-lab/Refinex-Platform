package cn.refinex.system.application.command;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询数据资源列表命令
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryDrsListCommand extends PageRequest {

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 所属组织ID(平台级为0)
     */
    private Long ownerEstabId;

    /**
     * 数据归属 0平台 1租户
     */
    private Integer dataOwnerType;

    /**
     * 关键字（数据资源编码/名称）
     */
    private String keyword;
}
