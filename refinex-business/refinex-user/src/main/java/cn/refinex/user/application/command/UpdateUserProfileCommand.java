package cn.refinex.user.application.command;

import lombok.Data;

import java.time.LocalDate;

/**
 * 更新用户资料命令
 *
 * @author refinex
 */
@Data
public class UpdateUserProfileCommand {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 当前企业ID（用于返回用户上下文）
     */
    private Long estabId;

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
     * 性别
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;
}
