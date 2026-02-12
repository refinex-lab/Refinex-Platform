package cn.refinex.job.autoconfigure;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * XXL-JOB 自动配置类
 *
 * @author refinex
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(XxlJobSpringExecutor.class)
@EnableConfigurationProperties(XxlJobProperties.class)
@ConditionalOnProperty(prefix = "refinex.job.xxl", name = "enabled", havingValue = "true")
public class XxlJobAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public XxlJobSpringExecutor xxlJobExecutor(XxlJobProperties properties, Environment environment) {
        log.info(">>>>>>>>>>> XXL-JOB Init Start: AdminAddresses={}", properties.getAdminAddresses());

        XxlJobSpringExecutor xxlJobExecutor = new XxlJobSpringExecutor();

        // 1. 调度中心地址
        xxlJobExecutor.setAdminAddresses(properties.getAdminAddresses());

        // 2. 执行器 AppName (优先配置 -> 默认 Spring 应用名)
        String appName = StringUtils.hasText(properties.getAppName())
                ? properties.getAppName()
                : environment.getProperty("spring.application.name");

        if (!StringUtils.hasText(appName)) {
            throw new IllegalArgumentException("XXL-JOB AppName cannot be empty. Check 'refinex.job.xxl.app-name' or 'spring.application.name'");
        }
        xxlJobExecutor.setAppname(appName);

        // 3. 网络配置 (IP/Port)
        if (StringUtils.hasText(properties.getIp())) {
            xxlJobExecutor.setIp(properties.getIp());
        }
        // 如果端口 > 0 才设置，否则走 XXL-JOB 默认逻辑（自动找端口）
        if (properties.getPort() > 0) {
            xxlJobExecutor.setPort(properties.getPort());
        }

        // 4. 安全与日志
        xxlJobExecutor.setAccessToken(properties.getAccessToken());
        xxlJobExecutor.setLogPath(properties.getLogPath());
        xxlJobExecutor.setLogRetentionDays(properties.getLogRetentionDays());

        log.info(">>>>>>>>>>> XXL-JOB Init Success. AppName={}", appName);
        return xxlJobExecutor;
    }
}
