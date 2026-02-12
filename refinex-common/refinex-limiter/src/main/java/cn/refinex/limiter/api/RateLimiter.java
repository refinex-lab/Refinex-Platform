package cn.refinex.limiter.api;

/**
 * 统一限流接口
 * <p>
 * 定义通用的限流行为，屏蔽底层实现（Redis/Sentinel/Guava）。
 *
 * @author refinex
 */
public interface RateLimiter {

    /**
     * 尝试获取许可（非阻塞）
     *
     * @param key        限流资源 Key (如: "order_create_ip:127.0.0.1")
     * @param limit      限流阈值 (如: 10)
     * @param windowSize 窗口大小，单位：秒 (如: 1)
     * @return true 表示通过，false 表示被限流
     */
    boolean tryAcquire(String key, int limit, int windowSize);
}
