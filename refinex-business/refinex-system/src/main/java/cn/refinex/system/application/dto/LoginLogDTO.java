package cn.refinex.system.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志 DTO
 *
 * @author refinex
 */
@Data
public class LoginLogDTO {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 企业名称
     */
    private String estabName;

    /**
     * 身份ID
     */
    private Long identityId;

    /**
     * 登录类型
     */
    private Integer loginType;

    /**
     * 登录来源
     */
    private Integer sourceType;

    /**
     * 登录是否成功
     */
    private Integer success;

    /**
     * 登录失败原因
     */
    private String failureReason;

    /**
     * IP地址
     */
    private String ip;

    /**
     * UserAgent
     */
    private String userAgent;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;
}
