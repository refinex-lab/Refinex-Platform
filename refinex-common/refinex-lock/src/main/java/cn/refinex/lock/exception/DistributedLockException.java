package cn.refinex.lock.exception;

/**
 * 分布式锁异常
 *
 * @author refinex
 */
public class DistributedLockException extends RuntimeException {

    /**
     * 错误信息
     *
     * @param message 错误信息
     */
    public DistributedLockException(String message) {
        super(message);
    }

    /**
     * 错误信息
     *
     * @param message 错误信息
     * @param cause   原始异常
     */
    public DistributedLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
