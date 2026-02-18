package cn.refinex.user.application.command;

import lombok.Data;

/**
 * 注册用户命令
 *
 * @author refinex
 */
@Data
public class RegisterUserCommand {

    /**
     * 注册类型
     */
    private Integer registerType;

    /**
     * 标识
     */
    private String identifier;

    /**
     * 密码
     */
    private String password;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 团队ID
     */
    private Long estabId;

    /**
     * 团队代码
     */
    private String estabCode;

    /**
     * 团队名称
     */
    private String estabName;

    /**
     * 是否创建团队
     */
    private Boolean createEstab;

    /**
     * 用户类型
     */
    private Integer userType;
}
