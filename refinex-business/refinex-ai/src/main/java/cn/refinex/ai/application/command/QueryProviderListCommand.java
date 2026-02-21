package cn.refinex.ai.application.command;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询供应商列表命令
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryProviderListCommand extends PageRequest {

    /**
     * 状态 1启用 0停用
     */
    private Integer status;

    /**
     * 关键词(匹配供应商名称/编码)
     */
    private String keyword;
}
