package cn.refinex.auth.api.vo;

import cn.refinex.api.user.model.context.LoginUser;
import lombok.Data;

/**
 * 登录响应
 *
 * @author refinex
 */
@Data
public class LoginResponse {

    /**
     * Token 信息
     */
    private TokenInfo token;

    /**
     * 登录用户信息
     */
    private LoginUser loginUser;
}
