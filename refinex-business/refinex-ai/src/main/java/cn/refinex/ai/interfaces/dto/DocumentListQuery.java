package cn.refinex.ai.interfaces.dto;

import cn.refinex.base.request.PageRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档列表查询
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentListQuery extends PageRequest {

    /**
     * 目录ID(0为根目录)
     */
    private Long folderId;

    /**
     * 状态 1正常 0禁用
     */
    @Min(value = 0, message = "状态取值非法")
    @Max(value = 1, message = "状态取值非法")
    private Integer status;

    /**
     * 关键字
     */
    @Size(max = 64, message = "关键字长度不能超过64个字符")
    private String keyword;
}
