package cn.refinex.base.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 单数据对象响应
 *
 * @param <T> 数据实体类型
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true) // 禁用继承自父类的 equals 和 hashCode 方法的生成
public class SingleResponse<T> extends BaseResponse {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 返回的数据实体
     */
    private T data;

    /**
     * 构建成功响应
     *
     * @param data 数据实体
     * @param <T>  类型泛型
     * @return SingleResponse
     */
    public static <T> SingleResponse<T> of(T data) {
        SingleResponse<T> response = new SingleResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    /**
     * 构建失败响应
     *
     * @param errorCode    错误码
     * @param errorMessage 错误描述
     * @return SingleResponse
     */
    public static <T> SingleResponse<T> fail(String errorCode, String errorMessage) {
        SingleResponse<T> response = new SingleResponse<>();
        response.setSuccess(false);
        response.setResponseCode(errorCode);
        response.setResponseMessage(errorMessage);
        return response;
    }
}
