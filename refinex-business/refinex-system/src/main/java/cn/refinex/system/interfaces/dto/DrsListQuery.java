package cn.refinex.system.interfaces.dto;

import cn.refinex.base.request.PageRequest;
import jakarta.validation.constraints.*;
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
     * 系统ID
     */
    @Positive(message = "系统ID必须大于0")
    private Long systemId;

    /**
     * 状态 1启用 2停用
     */
    @Min(value = 1, message = "状态取值非法")
    @Max(value = 2, message = "状态取值非法")
    private Integer status;

    /**
     * 资源类型 0数据库表 1接口资源 2文件 3其他
     */
    @Min(value = 0, message = "资源类型取值非法")
    @Max(value = 3, message = "资源类型取值非法")
    private Integer drsType;

    /**
     * 所属组织ID(平台级为0)
     */
    @PositiveOrZero(message = "所属组织ID不能小于0")
    private Long ownerEstabId;

    /**
     * 关键字（数据资源编码/名称）
     */
    @Size(max = 64, message = "关键字长度不能超过64个字符")
    private String keyword;
}
