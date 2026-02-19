package cn.refinex.system.interfaces.dto;

import lombok.Data;

/**
 * 企业地址列表查询参数
 *
 * @author refinex
 */
@Data
public class EstabAddressListQuery {

    /**
     * 地址类型
     */
    private Integer addrType;
}
