package cn.refinex.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 邮箱验证码发送请求
 *
 * @author refinex
 */
@Data
public class EmailSendRequest {

    /**
     * 邮箱地址
     */
    @NotBlank
    @Email
    @Size(max = 128)
    private String email;

    /**
     * 场景(login/register/reset)
     */
    @NotBlank
    @Size(max = 32)
    private String scene;

    /**
     * 组织ID（可选）
     */
    private Long estabId;
}
