package cn.refinex.system.application.command;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 查询登录日志列表命令
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryLoginLogListCommand extends PageRequest {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 登录是否成功
     */
    private Integer success;

    /**
     * 登录类型
     */
    private Integer loginType;

    /**
     * 登录来源
     */
    private Integer sourceType;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

}
