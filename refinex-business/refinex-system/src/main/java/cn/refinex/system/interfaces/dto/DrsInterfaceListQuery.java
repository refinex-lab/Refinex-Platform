package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 查询数据资源接口列表参数
 *
 * @author refinex
 */
@Data
public class DrsInterfaceListQuery {

    /**
     * 数据资源ID
     */
    @NotNull(message = "数据资源ID不能为空")
    @Positive(message = "数据资源ID必须大于0")
    private Long drsId;

    /**
     * 状态 1启用 2停用
     */
    @Min(value = 1, message = "状态取值非法")
    @Max(value = 2, message = "状态取值非法")
    private Integer status;

    /**
     * 关键字（接口编码/名称）
     */
    @Size(max = 64, message = "关键字长度不能超过64个字符")
    private String keyword;
}
