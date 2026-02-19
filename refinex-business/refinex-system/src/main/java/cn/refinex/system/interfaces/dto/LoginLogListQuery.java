package cn.refinex.system.interfaces.dto;

import cn.refinex.base.request.PageRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 查询登录日志列表参数
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginLogListQuery extends PageRequest {

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
     * 登录是否成功
     */
    @Min(value = 0, message = "登录结果取值非法")
    @Max(value = 1, message = "登录结果取值非法")
    private Integer success;

    /**
     * 登录类型
     */
    @Min(value = 1, message = "登录类型取值非法")
    @Max(value = 8, message = "登录类型取值非法")
    private Integer loginType;

    /**
     * 登录来源
     */
    @Min(value = 1, message = "登录来源取值非法")
    @Max(value = 5, message = "登录来源取值非法")
    private Integer sourceType;

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
