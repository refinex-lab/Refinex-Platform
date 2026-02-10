package cn.refinex.base.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * 分页数据对象响应
 *
 * @param <T> 列表项数据类型
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true) // 禁用继承自父类的 equals 和 hashCode 方法的生成
public class PageResponse<T> extends MultiResponse<T> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private int currentPage;

    /**
     * 每页条数
     */
    private int pageSize;

    /**
     * 总页数
     */
    private int totalPage;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 构建分页成功响应
     *
     * @param data        数据列表
     * @param total       总记录数
     * @param pageSize    每页条数
     * @param currentPage 当前页码
     * @return PageResponse
     */
    public static <T> PageResponse<T> of(List<T> data, long total, int pageSize, int currentPage) {
        PageResponse<T> response = new PageResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setTotal(total);
        response.setPageSize(pageSize);
        response.setCurrentPage(currentPage);

        // 计算总页数
        if (pageSize > 0) {
            int totalPage = (int) (total / pageSize);
            response.setTotalPage(total % pageSize == 0 ? totalPage : totalPage + 1);
        } else {
            response.setTotalPage(0);
        }

        return response;
    }
}
