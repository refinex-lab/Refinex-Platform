package cn.refinex.user.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置当前用户密码请求
 *
 * @author refinex
 */
@Data
public class UserPasswordResetRequest {

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 128, message = "新密码长度需在6-128个字符之间")
    private String newPassword;
}
