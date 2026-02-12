package cn.refinex.limiter;

import cn.refinex.limiter.support.SlidingWindowRateLimiter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Sliding Window Rate Limiter Test
 *
 * @author refinex
 */
@SpringBootTest(classes = RateLimiterTestConfiguration.class)
@ActiveProfiles("test")
@Disabled("集成测试需要本地 Redis 环境，默认跳过")
class SlidingWindowRateLimiterTest {

    @Autowired
    private SlidingWindowRateLimiter rateLimiter;

    @Test
    void testBasicLimiting() {
        String key = "test_limit_" + UUID.randomUUID();
        int limit = 3;
        int window = 5; // 5秒内允许3次

        // 前3次应该通过
        Assertions.assertTrue(rateLimiter.tryAcquire(key, limit, window));
        Assertions.assertTrue(rateLimiter.tryAcquire(key, limit, window));
        Assertions.assertTrue(rateLimiter.tryAcquire(key, limit, window));

        // 第4次应该失败
        Assertions.assertFalse(rateLimiter.tryAcquire(key, limit, window));
    }

    @Test
    void testWindowReset() throws InterruptedException {
        String key = "test_window_" + UUID.randomUUID();
        // 1秒内允许1次
        Assertions.assertTrue(rateLimiter.tryAcquire(key, 1, 1));
        Assertions.assertFalse(rateLimiter.tryAcquire(key, 1, 1));

        // 等待窗口过期
        TimeUnit.SECONDS.sleep(2); // 等待2秒确保过期

        // 应该再次通过
        Assertions.assertTrue(rateLimiter.tryAcquire(key, 1, 1));
    }
}
