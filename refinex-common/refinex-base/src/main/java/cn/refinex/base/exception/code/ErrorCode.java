package cn.refinex.base.exception.code;

/**
 * 错误码顶层接口
 * <p>
 * 目的：提供获取错误码和错误消息的统一入口
 *
 * @author refinex
 */
public interface ErrorCode {

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    String getCode();

    /**
     * 获取错误消息
     *
     * @return 错误消息
     */
    String getMessage();
}
