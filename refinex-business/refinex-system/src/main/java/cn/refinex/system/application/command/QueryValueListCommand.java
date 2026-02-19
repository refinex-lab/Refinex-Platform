package cn.refinex.system.application.command;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询值集明细列表命令
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryValueListCommand extends PageRequest {

    /**
     * 值集编码
     */
    private String setCode;

    /**
     * 状态 1启用 2停用
     */
    private Integer status;

    /**
     * 关键字（值编码/名称）
     */
    private String keyword;
}
