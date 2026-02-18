package cn.refinex.auth.infrastructure.client.user;

import cn.refinex.api.user.model.dto.EstabResolveRequest;
import cn.refinex.api.user.model.dto.UserAuthSubjectDTO;
import cn.refinex.api.user.model.dto.UserAuthSubjectQuery;
import cn.refinex.api.user.model.dto.UserInfoQuery;
import cn.refinex.api.user.model.dto.UserLoginFailureCommand;
import cn.refinex.api.user.model.dto.UserLoginSuccessCommand;
import cn.refinex.api.user.model.dto.UserRegisterCommand;
import cn.refinex.api.user.model.dto.UserRegisterResult;
import cn.refinex.api.user.model.vo.UserInfo;
import cn.refinex.auth.domain.error.AuthErrorCode;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.SingleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 用户远程网关
 *
 * @author refinex
 */
@Component
@RequiredArgsConstructor
public class UserRemoteGateway {

    private final UserHttpClient userHttpClient;

    /**
     * 注册用户
     *
     * @param command 注册命令
     * @return 注册结果
     */
    public UserRegisterResult register(UserRegisterCommand command) {
        return invoke(() -> userHttpClient.register(command));
    }

    /**
     * 解析机构ID
     *
     * @param estabId     机构ID
     * @param estabCode   机构编号
     * @return 机构ID
     */
    public Long resolveEstabId(Long estabId, String estabCode) {
        EstabResolveRequest request = new EstabResolveRequest();
        request.setEstabId(estabId);
        request.setEstabCode(estabCode);
        return invoke(() -> userHttpClient.resolveEstab(request));
    }

    /**
     * 查询认证主体
     *
     * @param identityType  认证主体类型
     * @param identifier    认证主体标识
     * @param estabId       机构ID
     * @return 认证主体
     */
    public UserAuthSubjectDTO queryAuthSubject(Integer identityType, String identifier, Long estabId) {
        UserAuthSubjectQuery query = new UserAuthSubjectQuery();
        query.setIdentityType(identityType);
        query.setIdentifier(identifier);
        query.setEstabId(estabId);
        return invoke(() -> userHttpClient.authSubject(query));
    }

    /**
     * 标记登录成功
     *
     * @param userId    用户ID
     * @param identityId    认证主体ID
     * @param ip      登录IP
     */
    public void markLoginSuccess(Long userId, Long identityId, String ip) {
        UserLoginSuccessCommand command = new UserLoginSuccessCommand();
        command.setUserId(userId);
        command.setIdentityId(identityId);
        command.setIp(ip);
        invoke(() -> userHttpClient.loginSuccess(command));
    }

    /**
     * 标记登录失败
     *
     * @param userId      用户ID
     * @param threshold   尝试次数阈值
     * @param lockMinutes 锁定分钟数
     */
    public void markLoginFailure(Long userId, Integer threshold, Integer lockMinutes) {
        UserLoginFailureCommand command = new UserLoginFailureCommand();
        command.setUserId(userId);
        command.setThreshold(threshold);
        command.setLockMinutes(lockMinutes);
        invoke(() -> userHttpClient.loginFailure(command));
    }

    /**
     * 查询用户信息
     *
     * @param userId  用户ID
     * @param estabId 机构ID
     * @return 用户信息
     */
    public UserInfo queryUserInfo(Long userId, Long estabId) {
        UserInfoQuery query = new UserInfoQuery();
        query.setUserId(userId);
        query.setEstabId(estabId);
        return invoke(() -> userHttpClient.userInfo(query));
    }

    /**
     * 调用用户服务
     *
     * @param supplier  调用方法
     * @param <T>       返回结果类型
     * @return 调用结果
     */
    private <T> T invoke(Supplier<SingleResponse<T>> supplier) {
        try {
            return unwrap(supplier.get());
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException("调用用户服务失败", ex, AuthErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 解包响应
     *
     * @param response  响应
     * @param <T>       返回结果类型
     * @return 返回结果
     */
    private <T> T unwrap(SingleResponse<T> response) {
        if (response == null) {
            throw new BizException(AuthErrorCode.SYSTEM_ERROR);
        }
        if (Boolean.TRUE.equals(response.getSuccess())) {
            return response.getData();
        }
        throw toAuthException(response.getResponseCode(), response.getResponseMessage());
    }

    /**
     * 转换认证异常
     *
     * @param code    错误码
     * @param message 错误信息
     * @return 认证异常
     */
    private BizException toAuthException(String code, String message) {
        if ("USER_400".equals(code)) {
            return new BizException(message, AuthErrorCode.INVALID_PARAM);
        }
        if ("USER_404".equals(code)) {
            return new BizException(message, AuthErrorCode.USER_NOT_FOUND);
        }
        if ("USER_404_ID".equals(code)) {
            return new BizException(message, AuthErrorCode.IDENTITY_NOT_FOUND);
        }
        if ("USER_403".equals(code)) {
            return new BizException(message, AuthErrorCode.USER_DISABLED);
        }
        if ("USER_403_ID".equals(code)) {
            return new BizException(message, AuthErrorCode.IDENTITY_DISABLED);
        }
        if ("USER_423".equals(code)) {
            return new BizException(message, AuthErrorCode.USER_LOCKED);
        }
        if ("USER_409_ID".equals(code)) {
            return new BizException(message, AuthErrorCode.DUPLICATE_IDENTITY);
        }
        if ("USER_422_REG".equals(code)) {
            return new BizException(message, AuthErrorCode.REGISTER_TYPE_NOT_SUPPORTED);
        }
        if ("USER_404_ESTAB".equals(code)) {
            return new BizException(message, AuthErrorCode.ESTAB_NOT_FOUND);
        }
        return new BizException(message == null || message.isBlank() ? AuthErrorCode.SYSTEM_ERROR.getMessage() : message, AuthErrorCode.SYSTEM_ERROR);
    }
}
