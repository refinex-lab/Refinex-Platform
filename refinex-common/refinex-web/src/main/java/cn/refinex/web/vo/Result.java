package cn.refinex.web.vo;

import cn.refinex.base.response.SingleResponse;
import cn.refinex.base.response.code.ResponseCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * Web 层通用响应对象
 * <p>
 * 统一定义接口返回格式，所有 Controller 接口均应返回此对象。
 *
 * @author refinex
 */
@Getter
@Setter
@ToString
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 业务状态码 (例如: "SUCCESS", "BIZ_ERROR", "40001")
     */
    private String code;

    /**
     * 请求是否成功
     */
    private Boolean success;

    /**
     * 响应描述信息 (用于展示给用户或开发者调试)
     */
    private String message;

    /**
     * 响应数据主体
     */
    private T data;

    /**
     * 无参构造
     */
    public Result() {
    }

    /**
     * 全参构造
     *
     * @param success 请求是否成功
     * @param code    业务状态码
     * @param message 响应描述信息
     * @param data    响应数据主体
     */
    public Result(Boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 适配 Base 层的 SingleResponse
     *
     * @param singleResponse 服务层响应
     */
    public Result(SingleResponse<T> singleResponse) {
        this.success = singleResponse.getSuccess();
        this.code = singleResponse.getResponseCode();
        this.message = singleResponse.getResponseMessage();
        this.data = singleResponse.getData();
    }

    /**
     * 构建成功响应 (无数据)
     *
     * @param <T> 响应数据类型
     * @return Result<T> 响应对象
     */
    public static <T> Result<T> success() {
        return new Result<>(true, ResponseCode.SUCCESS.name(), ResponseCode.SUCCESS.name(), null);
    }

    /**
     * 构建成功响应 (带数据)
     *
     * @param data 响应数据
     * @param <T>  响应数据类型
     * @return Result<T> 响应对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, ResponseCode.SUCCESS.name(), ResponseCode.SUCCESS.name(), data);
    }

    /**
     * 构建失败响应
     *
     * @param errorCode 错误码
     * @param errorMsg  错误描述
     * @param <T>       响应数据类型
     * @return Result<T> 响应对象
     */
    public static <T> Result<T> error(String errorCode, String errorMsg) {
        return new Result<>(false, errorCode, errorMsg, null);
    }

    /**
     * 构建失败响应 (基于枚举)
     *
     * @param responseCode 响应码枚举
     * @param <T>          响应数据类型
     * @return Result<T> 响应对象
     */
    public static <T> Result<T> error(ResponseCode responseCode) {
        return new Result<>(false, responseCode.name(), responseCode.name(), null);
    }
}
