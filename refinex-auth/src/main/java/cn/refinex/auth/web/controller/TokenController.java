package cn.refinex.auth.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.refinex.api.user.model.context.LoginUser;
import cn.refinex.auth.api.vo.TokenInfo;
import cn.refinex.auth.domain.error.AuthErrorCode;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.SingleResponse;
import cn.refinex.satoken.helper.LoginUserHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Token 接口
 *
 * @author refinex
 */
@RestController
@RequestMapping("/token")
public class TokenController {

    /**
     * 获取当前登录的token信息
     *
     * @return 单一结果
     */
    @GetMapping("/info")
    public SingleResponse<TokenInfo> tokenInfo() {
        if (!StpUtil.isLogin()) {
            throw new BizException(AuthErrorCode.USER_NOT_FOUND);
        }
        TokenInfo info = new TokenInfo();
        info.setTokenName(StpUtil.getTokenName());
        info.setTokenValue(StpUtil.getTokenValue());
        info.setTokenTimeout(StpUtil.getTokenTimeout());
        info.setActiveTimeout(StpUtil.getTokenActiveTimeout());
        info.setLoginId(StpUtil.getLoginId());
        return SingleResponse.of(info);
    }

    /**
     * 获取当前登录的用户信息
     *
     * @return 单一结果
     */
    @GetMapping("/current")
    public SingleResponse<LoginUser> currentUser() {
        if (!StpUtil.isLogin()) {
            throw new BizException(AuthErrorCode.USER_NOT_FOUND);
        }
        return SingleResponse.of(LoginUserHelper.getLoginUser());
    }
}
