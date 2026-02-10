package cn.refinex.base.response.code;

/**
 * 响应状态码枚举
 *
 * @author refinex
 */
public enum ResponseCode {

    /**
     * 请求处理成功
     */
    SUCCESS,

    /**
     * 数据重复或幂等冲突
     */
    DUPLICATED,

    /**
     * 输入参数校验不通过
     */
    ILLEGAL_ARGUMENT,

    /**
     * 系统内部未知异常
     */
    SYSTEM_ERROR,

    /**
     * 业务规则验证失败
     */
    BIZ_ERROR;
}
