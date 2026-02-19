package cn.refinex.system.domain.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错误日志实体
 *
 * @author refinex
 */
@Data
public class ErrorLogEntity {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误类型
     */
    private String errorType;

    /**
     * 错误级别
     */
    private Integer errorLevel;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 堆栈信息
     */
    private String stackTrace;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * HTTP方法
     */
    private String requestMethod;

    /**
     * 请求路径
     */
    private String requestPath;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;
}
