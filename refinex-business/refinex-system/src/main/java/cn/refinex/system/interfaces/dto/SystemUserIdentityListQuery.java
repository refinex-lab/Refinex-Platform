package cn.refinex.system.interfaces.dto;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户身份列表查询参数
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SystemUserIdentityListQuery extends PageRequest {
}
