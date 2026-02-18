package cn.refinex.api.user.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录成功更新命令
 *
 * @author refinex
 */
@Data
public class UserLoginSuccessCommand implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 身份ID
     */
    private Long identityId;

    /**
     * IP地址
     */
    private String ip;
}
