package cn.refinex.base.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 分页查询请求对象
 * <p>
 * 封装基础的分页参数：当前页码和每页条数。
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = false) // 禁用继承自父类的 equals 和 hashCode 方法的生成
public class PageRequest extends BaseRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码（从1开始）
     */
    private int currentPage = 1;

    /**
     * 每页结果数
     */
    private int pageSize = 10;
}
