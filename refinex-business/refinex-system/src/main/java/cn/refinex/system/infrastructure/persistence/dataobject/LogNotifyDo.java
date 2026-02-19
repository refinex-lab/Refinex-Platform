package cn.refinex.system.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知日志 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("log_notify")
public class LogNotifyDo extends BaseEntity {

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
     * 通知主题（邮件/站内信）
     */
    private String subject;

    /**
     * 内容摘要（脱敏）
     */
    private String contentDigest;

    /**
     * 服务提供商标识
     */
    private String provider;

    /**
     * 模板编码
     */
    private String templateCode;

    /**
     * 三方回执ID
     */
    private String bizId;

    /**
     * 发送状态 1成功 0失败
     */
    private Integer sendStatus;

    /**
     * 失败原因
     */
    private String errorMessage;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 请求IP
     */
    private String ip;

    /**
     * UserAgent
     */
    private String userAgent;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 企业ID
     */
    private Long estabId;
}
