package cn.refinex.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求
 *
 * @author refinex
 */
@Data
public class ResetPasswordRequest {

    /**
     * 重置方式（2手机号 3邮箱）
     */
    @NotNull
    private Integer resetType;

    /**
     * 重置标识（手机号/邮箱）
     */
    @NotBlank
    @Size(max = 191)
    private String identifier;

    /**
     * 重置验证码
     */
    @NotBlank
    @Size(max = 16)
    private String code;

    /**
     * 新密码
     */
    @NotBlank
    @Size(max = 128)
    private String newPassword;

    /**
     * 组织ID（可选）
     */
    private Long estabId;
}
