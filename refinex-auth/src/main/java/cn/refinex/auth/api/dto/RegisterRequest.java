package cn.refinex.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求
 *
 * @author refinex
 */
@Data
public class RegisterRequest {

    /**
     * 注册类型：1用户名 2手机号 3邮箱
     */
    @NotNull
    private Integer registerType;

    /**
     * 登录标识（用户名/手机号/邮箱）
     */
    @NotBlank
    @Size(max = 191)
    private String identifier;

    /**
     * 密码（用户名/邮箱注册必填）
     */
    @Size(max = 128)
    private String password;

    /**
     * 验证码（手机号/邮箱注册）
     */
    @Size(max = 16)
    private String code;

    /**
     * 显示名称
     */
    @Size(max = 64)
    private String displayName;

    /**
     * 昵称
     */
    @Size(max = 64)
    private String nickname;

    /**
     * 头像URL
     */
    @Size(max = 255)
    private String avatarUrl;

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
     * 组织名称（新建组织时使用）
     */
    @Size(max = 128)
    private String estabName;

    /**
     * 是否创建组织
     */
    private Boolean createEstab;

    /**
     * 用户类型（对应 def_user.user_type，可为空）
     */
    private Integer userType;
}
