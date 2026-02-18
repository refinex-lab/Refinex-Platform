package cn.refinex.api.user.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录失败更新命令
 *
 * @author refinex
 */
@Data
public class UserLoginFailureCommand implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 登录失败阈值
     */
    private Integer threshold;

    /**
     * 账号锁定分钟数
     */
    private Integer lockMinutes;
}
