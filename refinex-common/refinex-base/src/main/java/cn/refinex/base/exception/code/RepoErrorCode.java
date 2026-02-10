package cn.refinex.base.exception.code;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * 仓库层异常错误码
 * <p>
 * 用于标识数据库操作（如增删改查）过程中的异常情况
 *
 * @author refinex
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public enum RepoErrorCode implements ErrorCode {

    UNKNOWN_ERROR("UNKNOWN_ERROR", "未知错误"),
    INSERT_FAILED("INSERT_FAILED", "数据库插入失败"),
    UPDATE_FAILED("UPDATE_FAILED", "数据库更新失败"),
    DATA_NOT_EXIST("DATA_NOT_EXIST", "查询数据不存在");

    /**
     * 错误码标识
     */
    private final String code;

    /**
     * 错误描述信息
     */
    private final String message;

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    @Override
    public String getCode() {
        return this.code;
    }

    /**
     * 获取错误描述信息
     *
     * @return 错误描述信息
     */
    @Override
    public String getMessage() {
        return this.message;
    }
}
