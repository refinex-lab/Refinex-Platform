package cn.refinex.system.application.command;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 查询操作日志列表命令
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryOperateLogListCommand extends PageRequest {

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

}
