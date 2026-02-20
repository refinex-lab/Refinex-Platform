package cn.refinex.auth.interfaces.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.refinex.api.user.model.context.LoginUser;
import cn.refinex.api.user.model.vo.UserInfo;
import cn.refinex.auth.api.dto.SwitchEstabRequest;
import cn.refinex.auth.api.vo.TokenInfo;
import cn.refinex.auth.domain.error.AuthErrorCode;
import cn.refinex.auth.infrastructure.client.user.UserRemoteGateway;
import cn.refinex.auth.infrastructure.mapper.AuthRbacMapper;
import cn.refinex.base.exception.BizException;
import cn.refinex.web.vo.Result;
import jakarta.validation.Valid;
import cn.refinex.satoken.helper.LoginUserHelper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

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
    private final AuthRbacMapper authRbacMapper;

    /**
     * 获取当前登录的token信息
     *
     * @return Token 信息
     */
    @GetMapping("/info")
    public Result<TokenInfo> tokenInfo() {
        if (!StpUtil.isLogin()) {
            throw new BizException(AuthErrorCode.USER_NOT_FOUND);
        }

        TokenInfo info = new TokenInfo();
        info.setTokenName(StpUtil.getTokenName());
        info.setTokenValue(StpUtil.getTokenValue());
        info.setTokenTimeout(StpUtil.getTokenTimeout());
        info.setActiveTimeout(StpUtil.getTokenActiveTimeout());
        info.setLoginId(StpUtil.getLoginId());
        return Result.success(info);
    }

    /**
     * 获取当前登录的用户信息
     *
     * @return 当前登录用户
     */
    @GetMapping("/current")
    public Result<LoginUser> currentUser() {
        if (!StpUtil.isLogin()) {
            throw new BizException(AuthErrorCode.USER_NOT_FOUND);
        }

        return Result.success(LoginUserHelper.getLoginUser());
    }

    /**
     * 切换当前企业上下文
     *
     * @param request 切换请求
     * @return 更新后的登录用户
     */
    @PostMapping("/switch-estab")
    public Result<LoginUser> switchEstab(@Valid @RequestBody SwitchEstabRequest request) {
        if (!StpUtil.isLogin()) {
            throw new BizException(AuthErrorCode.USER_NOT_FOUND);
        }

        LoginUser loginUser = LoginUserHelper.getLoginUser();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BizException(AuthErrorCode.USER_NOT_FOUND);
        }

        UserInfo userInfo = userRemoteGateway.queryUserInfo(loginUser.getUserId(), request.getEstabId());

        List<String> roleCodes = authRbacMapper.findRoleCodes(loginUser.getUserId(), request.getEstabId());
        List<String> permissionCodes = authRbacMapper.findPermissionKeys(loginUser.getUserId(), request.getEstabId());

        loginUser.setUserCode(userInfo.getUserCode());
        loginUser.setUsername(userInfo.getUsername());
        loginUser.setDisplayName(userInfo.getDisplayName());
        loginUser.setNickname(userInfo.getNickname());
        loginUser.setAvatarUrl(userInfo.getAvatarUrl());
        if (userInfo.getUserType() != null) {
            loginUser.setUserType(userInfo.getUserType());
        }
        if (userInfo.getStatus() != null) {
            loginUser.setStatus(userInfo.getStatus());
        }
        loginUser.setPrimaryEstabId(userInfo.getPrimaryEstabId());
        loginUser.setEstabId(request.getEstabId());
        loginUser.setTeamId(userInfo.getPrimaryTeamId());
        loginUser.setEstabAdmin(Boolean.TRUE.equals(userInfo.getEstabAdmin()));
        loginUser.setRoleCodes(roleCodes == null ? Collections.emptyList() : roleCodes);
        loginUser.setPermissionCodes(permissionCodes == null ? Collections.emptyList() : permissionCodes);

        LoginUserHelper.setLoginUser(loginUser);
        return Result.success(loginUser);
    }
}
