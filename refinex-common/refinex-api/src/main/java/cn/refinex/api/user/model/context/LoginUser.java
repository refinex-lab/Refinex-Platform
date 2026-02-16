package cn.refinex.api.user.model.context;

import cn.refinex.api.user.enums.UserStatus;
import cn.refinex.api.user.enums.UserType;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录用户上下文对象
 * <p>
 * 用于鉴权、审计、缓存与上下游透传的轻量信息集合。
 *
 * @author refinex
 */
@Data
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Sa-Session 中存储的登录用户Key
     */
    public static final String SESSION_KEY = "loginUser";

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户编码
     */
    private String userCode;

    /**
     * 用户名（本地账号）
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
     * 头像
     */
    private String avatarUrl;

    /**
     * 用户类型
     */
    private UserType userType;

    /**
     * 用户状态
     */
    private UserStatus status;

    /**
     * 当前组织ID（登录上下文）
     */
    private Long estabId;

    /**
     * 当前团队ID（登录上下文）
     */
    private Long teamId;

    /**
     * 主组织ID（用户档案）
     */
    private Long primaryEstabId;

    /**
     * 是否组织管理员
     */
    private Boolean estabAdmin;

    /**
     * 角色编码列表
     */
    private List<String> roleCodes;

    /**
     * 权限编码列表
     */
    private List<String> permissionCodes;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
}
