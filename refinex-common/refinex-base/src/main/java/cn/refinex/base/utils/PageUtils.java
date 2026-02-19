package cn.refinex.base.utils;

import cn.refinex.base.response.PageResponse;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.List;

/**
 * 分页工具类
 *
 * @author refinex
 */
@UtilityClass
public class PageUtils {

    /**
     * 默认页码
     */
    public static final int DEFAULT_CURRENT_PAGE = 1;

    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 默认最大分页大小
     */
    public static final int DEFAULT_MAX_PAGE_SIZE = 200;

    /**
     * 规范化页码
     *
     * @param currentPage 页码
     * @return 合法页码
     */
    public static int normalizeCurrentPage(Integer currentPage) {
        if (currentPage == null || currentPage < 1) {
            return DEFAULT_CURRENT_PAGE;
        }
        return currentPage;
    }

    /**
     * 规范化分页大小
     *
     * @param pageSize        每页条数
     * @param defaultPageSize 默认每页条数
     * @param maxPageSize     最大每页条数
     * @return 合法分页大小
     */
    public static int normalizePageSize(Integer pageSize, int defaultPageSize, int maxPageSize) {
        int effectiveDefault = defaultPageSize > 0 ? defaultPageSize : DEFAULT_PAGE_SIZE;
        int effectiveMax = maxPageSize > 0 ? maxPageSize : DEFAULT_MAX_PAGE_SIZE;
        if (pageSize == null || pageSize < 1) {
            return effectiveDefault;
        }
        return Math.min(pageSize, effectiveMax);
    }

    /**
     * 按页码和大小切分列表并封装为分页响应
     *
     * @param source      原始数据列表
     * @param currentPage 页码
     * @param pageSize    每页条数
     * @param <T>         数据类型
     * @return 分页响应
     */
    public static <T> PageResponse<T> slice(List<T> source, int currentPage, int pageSize) {
        List<T> safeSource = source == null ? Collections.emptyList() : source;
        int safeCurrentPage = normalizeCurrentPage(currentPage);
        int safePageSize = normalizePageSize(pageSize, DEFAULT_PAGE_SIZE, Integer.MAX_VALUE);
        int fromIndex = (safeCurrentPage - 1) * safePageSize;
        if (fromIndex >= safeSource.size()) {
            return PageResponse.of(Collections.emptyList(), safeSource.size(), safePageSize, safeCurrentPage);
        }
        int toIndex = Math.min(fromIndex + safePageSize, safeSource.size());
        return PageResponse.of(safeSource.subList(fromIndex, toIndex), safeSource.size(), safePageSize, safeCurrentPage);
    }
}
