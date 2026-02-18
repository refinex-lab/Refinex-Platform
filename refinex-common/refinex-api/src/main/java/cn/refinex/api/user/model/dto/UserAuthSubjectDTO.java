package cn.refinex.api.user.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录主体信息
 *
 * @author refinex
 */
@Data
public class UserAuthSubjectDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户代码
     */
    private String userCode;

    /**
     * 用户名
     */
    private String username;

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
     * 用户类型
     */
    private Integer userType;

    /**
     * 用户状态
     */
    private Integer userStatus;

    /**
     * 主要组织ID
     */
    private Long primaryEstabId;

    /**
     * 登录失败次数
     */
    private Integer loginFailCount;

    /**
     * 账号锁定截止时间
     */
    private LocalDateTime lockUntil;

    /**
     * 身份ID
     */
    private Long identityId;

    /**
     * 身份状态
     */
    private Integer identityStatus;

    /**
     * 凭证
     */
    private String credential;

    /**
     * 团队ID
     */
    private Long teamId;

    /**
     * 是否组织管理员
     */
    private Boolean estabAdmin;
}
