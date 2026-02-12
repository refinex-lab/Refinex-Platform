package cn.refinex.lock.constant;

import lombok.experimental.UtilityClass;

/**
 * 分布式锁常量定义
 *
 * @author refinex
 */
@UtilityClass
public class DistributedLockConstant {

    /**
     * 默认 Key 占位符
     */
    public static final String NONE_KEY = "NONE";

    /**
     * 默认等待时间 (毫秒)
     * -1 代表不等待，获取不到立即失败（Fail-Fast）
     * 或者配合业务逻辑表示一直等待，建议显式设置
     */
    public static final long DEFAULT_WAIT_TIME = -1;

    /**
     * 默认租约时间 (毫秒)
     * -1 代表启用 Redisson 看门狗机制（WatchDog），自动续期（默认 30s，每 10s 续一次）
     */
    public static final long DEFAULT_LEASE_TIME = -1;
}
