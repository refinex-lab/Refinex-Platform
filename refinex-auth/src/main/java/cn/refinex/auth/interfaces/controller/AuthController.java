package cn.refinex.auth.interfaces.controller;

import cn.refinex.auth.api.dto.EmailSendRequest;
import cn.refinex.auth.api.dto.LoginRequest;
import cn.refinex.auth.api.dto.RegisterRequest;
import cn.refinex.auth.api.dto.ResetPasswordRequest;
import cn.refinex.auth.api.dto.SmsSendRequest;
import cn.refinex.auth.api.vo.LoginResponse;
import cn.refinex.auth.domain.enums.LoginSource;
import cn.refinex.auth.domain.model.LoginContext;
import cn.refinex.auth.application.service.AuthService;
import cn.refinex.base.utils.RequestUtils;
import cn.refinex.base.response.SingleResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口
 *
 * @author refinex
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 发送短信验证码
     *
     * @param request 短信验证码请求
     * @param httpRequest HTTP请求
     * @return 单一结果
     */
    @PostMapping("/sms/send")
    public SingleResponse<Void> sendSms(@Valid @RequestBody SmsSendRequest request, HttpServletRequest httpRequest) {
        authService.sendSmsCode(request, buildContext(null, httpRequest));
        return SingleResponse.of(null);
    }

    /**
     * 发送邮箱验证码
     *
     * @param request 邮箱验证码请求
     * @param httpRequest HTTP请求
     * @return 单一结果
     */
    @PostMapping("/email/send")
    public SingleResponse<Void> sendEmail(@Valid @RequestBody EmailSendRequest request, HttpServletRequest httpRequest) {
        authService.sendEmailCode(request, buildContext(null, httpRequest));
        return SingleResponse.of(null);
    }

    /**
     * 注册
     *
     * @param request 注册请求
     * @param httpRequest HTTP请求
     * @return 单一结果
     */
    @PostMapping("/register")
    public SingleResponse<Long> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        Long userId = authService.register(request, buildContext(null, httpRequest));
        return SingleResponse.of(userId);
    }

    /**
     * 重置密码
     *
     * @param request 重置密码请求
     * @param httpRequest HTTP请求
     * @return 单一结果
     */
    @PostMapping("/password/reset")
    public SingleResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
        authService.resetPassword(request, buildContext(null, httpRequest));
        return SingleResponse.of(null);
    }

    /**
     * 登录
     *
     * @param request 登录请求
     * @param httpRequest HTTP请求
     * @return 单一结果
     */
    @PostMapping("/login")
    public SingleResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginResponse response = authService.login(request, buildContext(request, httpRequest));
        return SingleResponse.of(response);
    }

    /**
     * 登出
     *
     * @return 单一结果
     */
    @PostMapping("/logout")
    public SingleResponse<Void> logout() {
        authService.logout();
        return SingleResponse.of(null);
    }

    /**
     * 构建登录上下文
     *
     * @param request 登录请求
     * @param httpRequest HTTP请求
     * @return 登录上下文
     */
    private LoginContext buildContext(LoginRequest request, HttpServletRequest httpRequest) {
        LoginContext context = new LoginContext();
        context.setIp(RequestUtils.getClientIp(httpRequest));
        context.setUserAgent(RequestUtils.getUserAgent(httpRequest));
        if (request != null) {
            context.setSource(LoginSource.of(request.getSourceType()));
            context.setClientId(request.getClientId());
            context.setDeviceId(request.getDeviceId());
        }
        if (context.getSource() == null) {
            context.setSource(LoginSource.WEB);
        }
        return context;
    }
}
