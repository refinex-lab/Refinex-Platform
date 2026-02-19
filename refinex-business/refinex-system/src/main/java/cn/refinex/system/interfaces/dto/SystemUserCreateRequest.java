package cn.refinex.system.interfaces.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 系统用户创建请求
 *
 * @author refinex
 */
@Data
public class SystemUserCreateRequest {

    /**
     * 用户编码
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
     * 性别
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 主要企业ID
     */
    private Long primaryEstabId;

    /**
     * 主要手机号
     */
    private String primaryPhone;

    /**
     * 手机号是否验证
     */
    private Integer phoneVerified;

    /**
     * 主要邮箱
     */
    private String primaryEmail;

    /**
     * 邮箱是否验证
     */
    private Integer emailVerified;

    /**
     * 备注
     */
    private String remark;
}
