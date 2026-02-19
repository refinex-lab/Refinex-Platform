package cn.refinex.system.interfaces.dto;

import cn.refinex.base.request.PageRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 查询通知日志列表参数
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NotifyLogListQuery extends PageRequest {

    /**
     * 通知通道 1短信 2邮件 3站内信
     */
    @Min(value = 1, message = "通知通道取值非法")
    @Max(value = 4, message = "通知通道取值非法")
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
     * 发送状态 0待发送 1成功 2失败
     */
    @Min(value = 0, message = "发送状态取值非法")
    @Max(value = 2, message = "发送状态取值非法")
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

}
