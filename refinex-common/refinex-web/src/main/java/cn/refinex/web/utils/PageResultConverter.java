package cn.refinex.web.utils;

import cn.refinex.base.response.PageResponse;
import cn.refinex.web.vo.PageResult;
import lombok.experimental.UtilityClass;

/**
 * 分页结果转换器
 * <p>
 * 将基础层(Base)的分页响应转换为 Web 层(VO)的分页结果。
 * <p>
 * 建议直接使用 {@link PageResult#PageResult(PageResponse)} 构造函数，此类仅作为兼容保留。
 *
 * @author refinex
 */
@UtilityClass
public class PageResultConverter {

    /**
     * 转换 PageResponse 到 PageResult
     *
     * @param pageResponse 基础层分页响应
     * @param <T>          数据类型
     * @return Web 层分页结果
     */
    public static <T> PageResult<T> convert(PageResponse<T> pageResponse) {
        if (pageResponse == null) {
            return PageResult.empty();
        }
        return new PageResult<>(pageResponse);
    }
}
