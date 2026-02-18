package cn.refinex.auth.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.refinex.api.user.model.context.LoginUser;
import cn.refinex.api.user.model.vo.UserInfo;
import cn.refinex.auth.api.vo.TokenInfo;
import cn.refinex.auth.domain.error.AuthErrorCode;
import cn.refinex.auth.infrastructure.client.user.UserRemoteGateway;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.SingleResponse;
import cn.refinex.satoken.helper.LoginUserHelper;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class TokenController {

    private final UserRemoteGateway userRemoteGateway;

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

    /**
     * 获取当前登录用户的详细信息（来自用户服务）
     *
     * @return 单一结果
     */
    @GetMapping("/user-info")
    public SingleResponse<UserInfo> currentUserInfo() {
        if (!StpUtil.isLogin()) {
            throw new BizException(AuthErrorCode.USER_NOT_FOUND);
        }
        LoginUser loginUser = LoginUserHelper.getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BizException(AuthErrorCode.USER_NOT_FOUND);
        }
        UserInfo userInfo = userRemoteGateway.queryUserInfo(loginUser.getUserId(), loginUser.getEstabId());
        return SingleResponse.of(userInfo);
    }
}
