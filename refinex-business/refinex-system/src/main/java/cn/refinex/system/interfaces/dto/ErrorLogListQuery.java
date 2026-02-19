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
 * 查询错误日志列表参数
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ErrorLogListQuery extends PageRequest {

    /**
     * 服务名
     */
    @Size(max = 64, message = "服务名长度不能超过64个字符")
    private String serviceName;

    /**
     * 错误级别
     */
    @Min(value = 1, message = "错误级别取值非法")
    @Max(value = 4, message = "错误级别取值非法")
    private Integer errorLevel;

    /**
     * 请求路径
     */
    @Size(max = 255, message = "请求路径长度不能超过255个字符")
    private String requestPath;

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
