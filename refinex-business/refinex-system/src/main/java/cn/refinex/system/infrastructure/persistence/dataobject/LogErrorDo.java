package cn.refinex.system.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 错误日志 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("log_error")
public class LogErrorDo extends BaseEntity {

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
}
