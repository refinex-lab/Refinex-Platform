package cn.refinex.system.interfaces.dto;

import lombok.Data;

/**
 * 企业列表查询参数
 *
 * @author refinex
 */
@Data
public class EstabListQuery {

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
