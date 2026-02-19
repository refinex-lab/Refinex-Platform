package cn.refinex.system.interfaces.dto;

import cn.refinex.base.request.PageRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询值集列表参数
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ValueSetListQuery extends PageRequest {

    /**
     * 状态 1启用 2停用
     */
    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status;

    /**
     * 关键字（值集编码/名称）
     */
    @Size(max = 64, message = "关键字长度不能超过64个字符")
    private String keyword;
}
