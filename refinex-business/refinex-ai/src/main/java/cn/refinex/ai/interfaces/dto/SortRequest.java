package cn.refinex.ai.interfaces.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 排序请求
 *
 * @author refinex
 */
@Data
public class SortRequest {

    /**
     * 排序项列表
     */
    @Valid
    @NotNull(message = "排序项不能为空")
    private List<SortItem> items;

    /**
     * 排序项
     */
    @Data
    public static class SortItem {

        /**
         * 主键ID
         */
        @NotNull(message = "ID不能为空")
        private Long id;

        /**
         * 类型
         */
        @NotBlank(message = "类型不能为空")
        private String type;

        /**
         * 排序值
         */
        @NotNull(message = "排序值不能为空")
        private Integer sort;
    }
}
