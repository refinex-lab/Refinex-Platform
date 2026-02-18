package cn.refinex.auth.infrastructure.client.user;

import cn.refinex.api.user.model.dto.EstabResolveRequest;
import cn.refinex.api.user.model.dto.UserAuthSubjectDTO;
import cn.refinex.api.user.model.dto.UserAuthSubjectQuery;
import cn.refinex.api.user.model.dto.UserInfoQuery;
import cn.refinex.api.user.model.dto.UserLoginFailureCommand;
import cn.refinex.api.user.model.dto.UserLoginSuccessCommand;
import cn.refinex.api.user.model.dto.UserRegisterCommand;
import cn.refinex.api.user.model.dto.UserRegisterResult;
import cn.refinex.api.user.model.dto.UserResetPasswordCommand;
import cn.refinex.api.user.model.vo.UserInfo;
import cn.refinex.base.response.SingleResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * 用户服务 HTTP 客户端
 *
 * @author refinex
 */
@HttpExchange("/internal/users")
public interface UserHttpClient {

    /**
     * 注册用户
     *
     * @param command 注册命令
     * @return 注册结果
     */
    @PostExchange("/register")
    SingleResponse<UserRegisterResult> register(@RequestBody UserRegisterCommand command);

    /**
     * 解析组织
     *
     * @param request 解析请求
     * @return 组织ID
     */
    @PostExchange("/estab/resolve")
    SingleResponse<Long> resolveEstab(@RequestBody EstabResolveRequest request);

    /**
     * 认证用户
     *
     * @param query 认证查询
     * @return 认证结果
     */
    @PostExchange("/auth/subject")
    SingleResponse<UserAuthSubjectDTO> authSubject(@RequestBody UserAuthSubjectQuery query);

    /**
     * 登录成功
     *
     * @param command 登录成功命令
     * @return 登录成功结果
     */
    @PostExchange("/auth/login/success")
    SingleResponse<Void> loginSuccess(@RequestBody UserLoginSuccessCommand command);

    /**
     * 登录失败
     *
     * @param command 登录失败命令
     * @return 登录失败结果
     */
    @PostExchange("/auth/login/failure")
    SingleResponse<Void> loginFailure(@RequestBody UserLoginFailureCommand command);

    /**
     * 重置密码
     *
     * @param command 重置密码命令
     * @return 操作结果
     */
    @PostExchange("/auth/password/reset")
    SingleResponse<Void> resetPassword(@RequestBody UserResetPasswordCommand command);

    /**
     * 获取用户信息
     *
     * @param query 用户信息查询
     * @return 用户信息
     */
    @PostExchange("/info")
    SingleResponse<UserInfo> userInfo(@RequestBody UserInfoQuery query);
}
