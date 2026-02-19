package cn.refinex.api.user.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户管理-更新用户命令
 *
 * @author refinex
 */
@Data
public class UserManageUpdateCommand implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

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
     * 性别 0未知 1男 2女 3其他
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 用户类型 0平台 1租户 2合作方
     */
    private Integer userType;

    /**
     * 状态 1启用 2停用 3锁定
     */
    private Integer status;

    /**
     * 主组织ID
     */
    private Long primaryEstabId;

    /**
     * 主手机号
     */
    private String primaryPhone;

    /**
     * 手机号是否验证 1是 0否
     */
    private Integer phoneVerified;

    /**
     * 主邮箱
     */
    private String primaryEmail;

    /**
     * 邮箱是否验证 1是 0否
     */
    private Integer emailVerified;

    /**
     * 备注
     */
    private String remark;
}
