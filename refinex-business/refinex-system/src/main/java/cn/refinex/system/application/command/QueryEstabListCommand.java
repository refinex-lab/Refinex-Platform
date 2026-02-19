package cn.refinex.system.application.command;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业列表查询命令
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryEstabListCommand extends PageRequest {

    /**
     * 状态
     */
    private Integer status;

    /**
     * 企业类型
     */
    private Integer estabType;

    /**
     * 关键字
     */
    private String keyword;
}
