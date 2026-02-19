package cn.refinex.system.application.command;

import cn.refinex.base.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 查询通知日志列表命令
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryNotifyLogListCommand extends PageRequest {

    /**
     * 通知通道 1短信 2邮件 3站内信
     */
    private Integer channelType;

    /**
     * 业务场景编码
     */
    private String sceneCode;

    /**
     * 接收目标（手机号/邮箱/用户ID）
     */
    private String receiver;

    /**
     * 发送状态 1成功 0失败
     */
    private Integer sendStatus;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

}
