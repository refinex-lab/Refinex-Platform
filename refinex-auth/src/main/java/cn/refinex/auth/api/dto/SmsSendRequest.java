package cn.refinex.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 短信验证码发送请求
 *
 * @author refinex
 */
@Data
public class SmsSendRequest {

    /**
     * 手机号
     */
    @NotBlank
    @Size(max = 32)
    private String phone;

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
