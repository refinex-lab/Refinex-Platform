package cn.refinex.base.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * 列表数据对象响应
 *
 * @param <T> 列表项数据类型
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true) // 禁用继承自父类的 equals 和 hashCode 方法的生成
public class MultiResponse<T> extends BaseResponse {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 返回的数据列表
     */
    private List<T> data;

    /**
     * 构建成功响应
     *
     * @param data 数据列表
     * @param <T>  类型泛型
     * @return MultiResponse
     */
    public static <T> MultiResponse<T> of(List<T> data) {
        MultiResponse<T> response = new MultiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }
}
