package cn.refinex.system.application.command;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询角色列表命令
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryRoleListCommand extends PageRequest {

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 关键字（角色编码/名称）
     */
    private String keyword;
}
