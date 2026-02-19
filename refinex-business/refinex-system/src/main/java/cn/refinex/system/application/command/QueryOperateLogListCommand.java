package cn.refinex.system.application.command;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 查询操作日志列表命令
 *
 * @author refinex
 */
@Data
public class QueryOperateLogListCommand {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 操作是否成功
     */
    private Integer success;

    /**
     * 模块编码
     */
    private String moduleCode;

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
