package cn.refinex.auth.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求
 *
 * @author refinex
 */
@Data
public class LoginRequest {

    /**
     * 登录方式（参考 LoginType.code）
     */
    @NotNull
    private Integer loginType;

    /**
     * 登录标识（用户名/手机号/邮箱/openid等）
     */
    @Size(max = 191)
    private String identifier;

    /**
     * 密码
     */
    @Size(max = 128)
    private String password;

    /**
     * 验证码（短信/邮箱）
     */
    @Size(max = 16)
    private String code;

    /**
     * 组织ID（可选）
     */
    private Long estabId;

    /**
     * 组织编码（可选）
     */
    @Size(max = 64)
    private String estabCode;

    /**
     * 登录来源（参考 LoginSource.code）
     */
    private Integer sourceType;

    /**
     * 客户端ID
     */
    @Size(max = 64)
    private String clientId;

    /**
     * 设备ID
     */
    @Size(max = 128)
    private String deviceId;
}
