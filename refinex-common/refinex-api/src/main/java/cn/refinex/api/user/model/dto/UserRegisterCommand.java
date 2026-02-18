package cn.refinex.api.user.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册命令
 *
 * @author refinex
 */
@Data
public class UserRegisterCommand implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 注册类型
     */
    private Integer registerType;

    /**
     * 标识符
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
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 企业ID
     */
    private Long estabId;

    /**
     * 企业代码
     */
    private String estabCode;

    /**
     * 企业名称
     */
    private String estabName;

    /**
     * 是否创建企业
     */
    private Boolean createEstab;

    /**
     * 用户类型
     */
    private Integer userType;
}
