package cn.refinex.system.interfaces.dto;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业地址列表查询参数
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EstabAddressListQuery extends PageRequest {

    /**
     * 地址类型
     */
    private Integer addrType;
}
