package cn.refinex.es.autoconfigure;

import org.dromara.easyes.spring.annotation.EsMapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Easy-ES 自动配置类
 * <p>
 * 主要职责：
 * 1. 扫描 ES Mapper 接口。
 * 2. 启用 Easy-ES 自动配置。
 *
 * @author refinex
 */
@AutoConfiguration
// 关键：扫描所有模块下的 infrastructure.es.mapper 包
@EsMapperScan("cn.refinex.**.infrastructure.es.mapper")
@ConditionalOnProperty(prefix = "refinex.elasticsearch", name = "enabled", havingValue = "true")
public class EsAutoConfiguration {

    // Easy-ES 的核心配置 (GlobalConfig 等) 通常由 Starter 自动加载，
    // 这里仅作为 Mapper 扫描的入口。
    // 如果需要自定义策略 (如 UUID 生成策略、自定义拦截器)，可在此处定义 @Bean。
}
