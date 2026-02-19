package cn.refinex.system.interfaces.dto;

import cn.refinex.base.request.PageRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 查询操作日志列表参数
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperateLogListQuery extends PageRequest {

    /**
     * 用户ID
     */
    @Positive(message = "用户ID必须大于0")
    private Long userId;

    /**
     * 企业ID
     */
    @Positive(message = "企业ID必须大于0")
    private Long estabId;

    /**
     * 操作是否成功
     */
    @Min(value = 0, message = "操作结果取值非法")
    @Max(value = 1, message = "操作结果取值非法")
    private Integer success;

    /**
     * 模块编码
     */
    @Size(max = 64, message = "模块编码长度不能超过64个字符")
    private String moduleCode;

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
