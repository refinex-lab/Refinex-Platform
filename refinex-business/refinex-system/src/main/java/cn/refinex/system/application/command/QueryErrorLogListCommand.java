package cn.refinex.system.application.command;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 查询错误日志列表命令
 *
 * @author refinex
 */
@Data
public class QueryErrorLogListCommand {

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 错误级别
     */
    private Integer errorLevel;

    /**
     * 请求路径
     */
    private String requestPath;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 限制条数
     */
    private Integer limit;
}
