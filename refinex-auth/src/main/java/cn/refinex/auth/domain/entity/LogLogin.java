package cn.refinex.auth.domain.entity;

import cn.refinex.datasource.domain.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录日志
 *
 * @author refinex
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("log_login")
public class LogLogin extends BaseEntity {

    private Long userId;

    private Long estabId;

    private Long identityId;

    private Integer loginType;

    private Integer sourceType;

    private Integer success;

    private String failureReason;

    private String ip;

    private String userAgent;

    private String deviceId;

    private String clientId;

    private String requestId;
}
