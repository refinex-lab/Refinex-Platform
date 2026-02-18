package cn.refinex.auth.application.service;

import cn.refinex.auth.api.dto.EmailSendRequest;
import cn.refinex.auth.api.dto.LoginRequest;
import cn.refinex.auth.api.dto.RegisterRequest;
import cn.refinex.auth.api.dto.ResetPasswordRequest;
import cn.refinex.auth.api.dto.SmsSendRequest;
import cn.refinex.auth.api.vo.LoginResponse;
import cn.refinex.auth.domain.model.LoginContext;

/**
 * 认证服务接口
 *
 * @author refinex
 */
public interface AuthService {

    /**
     * 发送短信验证码
     *
     * @param request  发送请求
     * @param context 登录上下文
     */
    void sendSmsCode(SmsSendRequest request, LoginContext context);

    /**
     * 发送邮箱验证码
     *
     * @param request  发送请求
     * @param context 登录上下文
     */
    void sendEmailCode(EmailSendRequest request, LoginContext context);

    /**
     * 重置密码
     *
     * @param request 重置密码请求
     * @param context 登录上下文
     */
    void resetPassword(ResetPasswordRequest request, LoginContext context);

    /**
     * 注册
     *
     * @param request  注册请求
     * @param context 登录上下文
     * @return 用户ID
     */
    Long register(RegisterRequest request, LoginContext context);

    /**
     * 登录
     *
     * @param request  登录请求
     * @param context 登录上下文
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request, LoginContext context);

    /**
     * 登出
     */
    void logout();
}
