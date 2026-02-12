package cn.refinex.job.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * XXL-JOB 配置属性
 * <p>
 * 对应配置文件前缀: refinex.job.xxl
 *
 * @author refinex
 */
@Data
@ConfigurationProperties(prefix = "refinex.job.xxl")
public class XxlJobProperties {

    /**
     * 是否启用 XXL-JOB 执行器
     */
    private boolean enabled = false;

    /**
     * 调度中心地址
     * 如调度中心集群部署存在多个地址，则用逗号分隔。
     * 示例: http://127.0.0.1:8080/xxl-job-admin
     */
    private String adminAddresses;

    /**
     * 执行器 AccessToken (安全性校验)
     * 需与调度中心配置保持一致
     */
    private String accessToken;

    /**
     * 执行器 AppName
     * 默认读取 spring.application.name
     */
    private String appName;

    /**
     * 执行器注册 IP
     * 默认为空，表示自动获取 IP
     */
    private String ip;

    /**
     * 执行器端口
     * 默认 0 或 -1 表示自动寻找可用端口
     * 建议在 K8s 环境下显式指定，或使用 server.port + offset 策略
     */
    private int port = 9999;

    /**
     * 执行器运行日志文件存储磁盘路径
     * 建议配置为挂载卷路径，如 /var/logs/xxl-job/jobhandler
     */
    private String logPath = "/data/applogs/xxl-job/jobhandler";

    /**
     * 调度中心日志保存天数
     * 过期日志自动清理，默认 30 天
     */
    private int logRetentionDays = 30;
}
