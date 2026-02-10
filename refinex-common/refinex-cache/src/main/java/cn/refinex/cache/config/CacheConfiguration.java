package cn.refinex.cache.config;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.context.annotation.Configuration;

/**
 * JetCache 缓存配置类
 * <p>
 * 主要作用：
 * 1. 开启 JetCache 的注解支持（如 @Cached, @CreateCache）。
 * 2. 指定缓存注解扫描的包路径。
 *
 * @author refinex
 */
@Configuration
@EnableMethodCache(basePackages = "cn.refinex") // 扫描项目根包
public class CacheConfiguration {
    // 配置类为空，仅用于开启 JetCache 注解支持
}
