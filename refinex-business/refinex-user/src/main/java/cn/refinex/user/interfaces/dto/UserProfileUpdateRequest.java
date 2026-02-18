package cn.refinex.user.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新用户资料请求
 *
 * @author refinex
 */
@Data
public class UserProfileUpdateRequest {

    /**
     * 显示名称
     */
    @NotBlank(message = "显示名称不能为空")
    @Size(max = 64, message = "显示名称长度不能超过64个字符")
    private String displayName;

    /**
     * 昵称
     */
    @Size(max = 64, message = "昵称长度不能超过64个字符")
    private String nickname;

    /**
     * 头像地址
     */
    @Size(max = 255, message = "头像地址长度不能超过255个字符")
    private String avatarUrl;

    /**
     * 性别 0未知 1男 2女 3其他
     */
    @Min(value = 0, message = "性别取值非法")
    @Max(value = 3, message = "性别取值非法")
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;
}
