package cn.refinex.system.infrastructure.persistence.dataobject;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志 DO
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("log_operate")
public class LogOperateDo extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 模块编码
     */
    private String moduleCode;

    /**
     * 操作名称/动作
     */
    private String operation;

    /**
     * 目标类型
     */
    private String targetType;

    /**
     * 目标ID
     */
    private String targetId;

    /**
     * 操作是否成功
     */
    private Integer success;

    /**
     * 失败原因
     */
    private String failReason;

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
     * 响应摘要
     */
    private String responseBody;

    /**
     * IP地址
     */
    private String ip;

    /**
     * UserAgent
     */
    private String userAgent;

    /**
     * 耗时（毫秒）
     */
    private Integer durationMs;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 链路SpanID
     */
    private String spanId;
}
