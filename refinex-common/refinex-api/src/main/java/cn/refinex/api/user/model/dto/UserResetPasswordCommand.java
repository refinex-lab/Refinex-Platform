package cn.refinex.api.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码命令
 *
 * @author refinex
 */
@Data
public class UserResetPasswordCommand {

    /**
     * 校验身份类型（2手机号 3邮箱）
     */
    @NotNull
    private Integer verifyIdentityType;

    /**
     * 校验标识（手机号/邮箱）
     */
    @NotBlank
    @Size(max = 191)
    private String identifier;

    /**
     * 新密码（明文，由用户服务加密）
     */
    @NotBlank
    @Size(max = 128)
    private String newPassword;

    /**
     * 组织ID（可选）
     */
    private Long estabId;
}
