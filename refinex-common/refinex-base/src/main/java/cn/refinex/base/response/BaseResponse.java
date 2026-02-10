package cn.refinex.base.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 基础响应对象
 * <p>
 * 包含基础的执行结果状态、错误码及错误信息。
 *
 * @author refinex
 */
@Data
public class BaseResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 执行是否成功
     */
    private Boolean success;

    /**
     * 响应错误码
     */
    private String responseCode;

    /**
     * 响应描述信息
     */
    private String responseMessage;
}
