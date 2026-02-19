package cn.refinex.system.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志 VO
 *
 * @author refinex
 */
@Data
public class LoginLogVO {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 企业ID
     */
    private Long estabId;

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
