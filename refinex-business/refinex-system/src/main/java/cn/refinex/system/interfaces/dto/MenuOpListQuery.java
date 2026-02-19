package cn.refinex.system.interfaces.dto;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单操作列表查询参数
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuOpListQuery extends PageRequest {
}
