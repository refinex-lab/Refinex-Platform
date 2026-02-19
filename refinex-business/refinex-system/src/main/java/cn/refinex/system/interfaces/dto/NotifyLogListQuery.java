package cn.refinex.system.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 查询通知日志列表参数
 *
 * @author refinex
 */
@Data
public class NotifyLogListQuery {

    /**
     * 通知通道 1短信 2邮件 3站内信
     */
    @Min(value = 1, message = "通知通道取值非法")
    @Max(value = 3, message = "通知通道取值非法")
    private Integer channelType;

    /**
     * 业务场景编码
     */
    @Size(max = 32, message = "场景编码长度不能超过32个字符")
    private String sceneCode;

    /**
     * 接收目标（手机号/邮箱/用户ID）
     */
    @Size(max = 191, message = "接收方长度不能超过191个字符")
    private String receiver;

    /**
     * 发送状态 1成功 0失败
     */
    @Min(value = 0, message = "发送状态取值非法")
    @Max(value = 1, message = "发送状态取值非法")
    private Integer sendStatus;

    /**
     * 开始时间
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    /**
     * 限制条数
     */
    @Min(value = 1, message = "limit 最小为1")
    @Max(value = 200, message = "limit 最大为200")
    private Integer limit;
}
