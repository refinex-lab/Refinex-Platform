package cn.refinex.web.vo;

import cn.refinex.base.response.PageResponse;
import cn.refinex.base.response.code.ResponseCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.util.Collections;
import java.util.List;

/**
 * Web 层分页响应对象
 * <p>
 * 用于返回列表数据及分页元数据。
 *
 * @author refinex
 */
@Getter
@Setter
@ToString(callSuper = true)
public class PageResult<T> extends Result<List<T>> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 总页数
     */
    private int totalPage;

    /**
     * 当前页码 (通常从1开始)
     */
    private int page;

    /**
     * 每页记录数
     */
    private int size;

    /**
     * 无参构造
     */
    public PageResult() {
        super();
    }

    /**
     * 全参构造
     *
     * @param success 请求是否成功
     * @param code    业务状态码
     * @param message 响应描述信息
     * @param data    响应数据主体
     * @param total   总记录数
     * @param page    当前页码
     * @param size    每页记录数
     */
    public PageResult(Boolean success, String code, String message, List<T> data, long total, int page, int size) {
        super(success, code, message, data);
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPage = calculateTotalPage(total, size);
    }

    /**
     * 适配 Base 层的 PageResponse
     *
     * @param pageResponse 服务层分页响应
     */
    public PageResult(PageResponse<T> pageResponse) {
        // 复用父类逻辑初始化基础字段
        super(pageResponse.getSuccess(), pageResponse.getResponseCode(), pageResponse.getResponseMessage(), pageResponse.getData());

        this.total = pageResponse.getTotal();
        this.page = pageResponse.getCurrentPage();
        this.size = pageResponse.getPageSize();
        // 优先使用 Base 层计算好的 totalPage，如果没有则重新计算
        this.totalPage = pageResponse.getTotalPage() > 0 ? pageResponse.getTotalPage() : calculateTotalPage(total, size);
    }

    /**
     * 静态成功构造器
     *
     * @param data  当前页数据列表
     * @param total 总记录数
     * @param page  当前页码
     * @param size  每页大小
     * @return PageResult<T> 分页响应对象
     */
    public static <T> PageResult<T> success(List<T> data, long total, int page, int size) {
        return new PageResult<>(true, ResponseCode.SUCCESS.name(), ResponseCode.SUCCESS.name(), data, total, page, size);
    }

    /**
     * 静态空列表构造器 (用于查询为空的情况)
     *
     * @return PageResult<T> 分页响应对象
     */
    public static <T> PageResult<T> empty() {
        // 这里的 1, 10 只是默认值，防止除零异常，前端展示为空列表即可
        return new PageResult<>(true, ResponseCode.SUCCESS.name(), ResponseCode.SUCCESS.name(), Collections.emptyList(), 0, 1, 10);
    }

    /**
     * 静态失败构造器
     * <p>
     * 修正：方法名改为 failure，避免与父类 Result.error(String, String) 冲突
     *
     * @param errorCode 错误码
     * @param errorMsg  错误信息
     * @return PageResult<T> 分页响应对象
     */
    public static <T> PageResult<T> failure(String errorCode, String errorMsg) {
        // 失败时总数、页码等置为 0
        return new PageResult<>(false, errorCode, errorMsg, null, 0, 0, 0);
    }

    /**
     * 计算总页数
     *
     * @param total 总记录数
     * @param size  每页大小
     * @return 总页数
     */
    private int calculateTotalPage(long total, int size) {
        if (size <= 0) return 0;
        return (int) ((total + size - 1) / size);
    }
}
