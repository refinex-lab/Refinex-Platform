package cn.refinex.auth.domain.model;

import cn.refinex.auth.domain.enums.LoginSource;
import cn.refinex.auth.domain.enums.LoginType;
import lombok.Data;

/**
 * 登录日志事件
 *
 * @author refinex
 */
@Data
public class LoginLogEvent {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 组织ID
     */
    private Long estabId;

    /**
     * 身份ID
     */
    private Long identityId;

    /**
     * 登录类型
     */
    private LoginType loginType;

    /**
     * 登录来源
     */
    private LoginSource source;

    /**
     * 登录是否成功
     */
    private boolean success;

    /**
     * 登录失败原因
     */
    private String failureReason;

    /**
     * 登录上下文
     */
    private LoginContext context;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 创建登录成功日志事件
     *
     * @param userId 用户ID
     * @param estabId 组织ID
     * @param identityId 身份ID
     * @param loginType 登录类型
     * @param source 登录来源
     * @param context 登录上下文
     * @return 登录日志事件
     */
    public static LoginLogEvent success(Long userId, Long estabId, Long identityId, LoginType loginType, LoginSource source, LoginContext context) {
        LoginLogEvent event = base(userId, estabId, identityId, loginType, source, context);
        event.setSuccess(true);
        return event;
    }

    /**
     * 创建登录失败日志事件
     *
     * @param userId 用户ID
     * @param estabId 组织ID
     * @param identityId 身份ID
     * @param loginType 登录类型
     * @param source 登录来源
     * @param failureReason 登录失败原因
     * @param context 登录上下文
     * @return 登录日志事件
     */
    public static LoginLogEvent failure(Long userId, Long estabId, Long identityId, LoginType loginType, LoginSource source, String failureReason, LoginContext context) {
        LoginLogEvent event = base(userId, estabId, identityId, loginType, source, context);
        event.setSuccess(false);
        event.setFailureReason(failureReason);
        return event;
    }

    /**
     * 创建基础登录日志事件
     *
     * @param userId 用户ID
     * @param estabId 组织ID
     * @param identityId 身份ID
     * @param loginType 登录类型
     * @param source 登录来源
     * @param context 登录上下文
     * @return 登录日志事件
     */
    private static LoginLogEvent base(Long userId, Long estabId, Long identityId, LoginType loginType, LoginSource source, LoginContext context) {
        LoginLogEvent event = new LoginLogEvent();
        event.setUserId(userId);
        event.setEstabId(estabId);
        event.setIdentityId(identityId);
        event.setLoginType(loginType);
        event.setSource(source);
        event.setContext(context);
        return event;
    }
}
