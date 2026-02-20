package cn.refinex.system.interfaces.dto;

import cn.refinex.base.request.PageRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询数据资源列表参数
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DrsListQuery extends PageRequest {

    /**
     * 状态 1启用 2停用
     */
    @Min(value = 1, message = "状态取值非法")
    @Max(value = 2, message = "状态取值非法")
    private Integer status;

    /**
     * 所属组织ID(平台级为0)
     */
    @PositiveOrZero(message = "所属组织ID不能小于0")
    private Long ownerEstabId;

    /**
     * 数据归属 0平台 1租户
     */
    @Min(value = 0, message = "数据归属取值非法")
    @Max(value = 1, message = "数据归属取值非法")
    private Integer dataOwnerType;

    /**
     * 关键字（数据资源编码/名称）
     */
    @Size(max = 64, message = "关键字长度不能超过64个字符")
    private String keyword;
}
