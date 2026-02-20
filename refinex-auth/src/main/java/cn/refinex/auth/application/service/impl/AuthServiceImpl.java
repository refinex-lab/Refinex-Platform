package cn.refinex.auth.application.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.refinex.api.user.enums.UserStatus;
import cn.refinex.api.user.enums.UserType;
import cn.refinex.api.user.model.context.LoginUser;
import cn.refinex.api.user.model.dto.UserAuthSubjectDTO;
import cn.refinex.api.user.model.dto.UserRegisterCommand;
import cn.refinex.api.user.model.dto.UserRegisterResult;
import cn.refinex.auth.api.dto.EmailSendRequest;
import cn.refinex.auth.api.dto.LoginRequest;
import cn.refinex.auth.api.dto.RegisterRequest;
import cn.refinex.auth.api.dto.ResetPasswordRequest;
import cn.refinex.auth.api.dto.SmsSendRequest;
import cn.refinex.auth.api.vo.LoginResponse;
import cn.refinex.auth.api.vo.TokenInfo;
import cn.refinex.auth.config.AuthProperties;
import cn.refinex.auth.domain.entity.DefEstabAuthPolicy;
import cn.refinex.auth.domain.entity.ScrRole;
import cn.refinex.auth.domain.entity.ScrRoleUser;
import cn.refinex.auth.domain.enums.LoginType;
import cn.refinex.auth.domain.enums.RegisterType;
import cn.refinex.auth.domain.error.AuthErrorCode;
import cn.refinex.auth.domain.model.LoginContext;
import cn.refinex.auth.domain.model.LoginLogContextHolder;
import cn.refinex.auth.domain.model.LoginLogEvent;
import cn.refinex.auth.infrastructure.client.user.UserRemoteGateway;
import cn.refinex.auth.infrastructure.mapper.AuthRbacMapper;
import cn.refinex.auth.infrastructure.mapper.DefEstabAuthPolicyMapper;
import cn.refinex.auth.infrastructure.mapper.ScrRoleMapper;
import cn.refinex.auth.infrastructure.mapper.ScrRoleUserMapper;
import cn.refinex.auth.infrastructure.security.AuthSecurityService;
import cn.refinex.auth.application.service.AuthService;
import cn.refinex.auth.infrastructure.persistence.service.LoginLogService;
import cn.refinex.auth.infrastructure.verification.VerificationCodeService;
import cn.refinex.base.exception.BizException;
import cn.refinex.satoken.helper.LoginUserHelper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 认证服务实现
 *
 * @author refinex
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String LIMIT_ONE = "LIMIT 1";

    private final AuthProperties authProperties;
    private final VerificationCodeService verificationCodeService;
    private final AuthSecurityService authSecurityService;
    private final PasswordEncoder passwordEncoder;
    private final LoginLogService loginLogService;
    private final UserRemoteGateway userRemoteGateway;
    private final DefEstabAuthPolicyMapper defEstabAuthPolicyMapper;
    private final ScrRoleMapper scrRoleMapper;
    private final ScrRoleUserMapper scrRoleUserMapper;
    private final AuthRbacMapper authRbacMapper;

    /**
     * 发送验证码
     *
     * @param request  验证码发送请求
     * @param context  登录上下文
     */
    @Override
    public void sendSmsCode(SmsSendRequest request, LoginContext context) {
        authSecurityService.checkSmsSend(request.getPhone(), context);

        if (request.getEstabId() != null) {
            AuthPolicy policy = resolveAuthPolicy(request.getEstabId());
            if (isForbidden(policy.smsLoginEnabled)) {
                throw new BizException(AuthErrorCode.ESTAB_LOGIN_FORBIDDEN);
            }
        }

        verificationCodeService.sendSmsCode(request.getPhone(), request.getScene());
    }

    /**
     * 发送邮箱验证码
     *
     * @param request  邮箱验证码发送请求
     * @param context  登录上下文
     */
    @Override
    public void sendEmailCode(EmailSendRequest request, LoginContext context) {
        authSecurityService.checkEmailSend(request.getEmail(), context);

        if (request.getEstabId() != null) {
            AuthPolicy policy = resolveAuthPolicy(request.getEstabId());
            if (isForbidden(policy.emailLoginEnabled)) {
                throw new BizException(AuthErrorCode.ESTAB_LOGIN_FORBIDDEN);
            }
        }

        verificationCodeService.sendEmailCode(request.getEmail(), request.getScene());
    }

    /**
     * 重置密码
     *
     * @param request 重置密码请求
     * @param context 登录上下文
     */
    @Override
    public void resetPassword(ResetPasswordRequest request, LoginContext context) {
        authSecurityService.checkLogin(request.getIdentifier(), context);

        RegisterType resetType = requireRegisterType(request.getResetType());
        if (resetType != RegisterType.PHONE && resetType != RegisterType.EMAIL) {
            throw new BizException(AuthErrorCode.INVALID_PARAM);
        }

        String identifier = request.getIdentifier() == null ? "" : request.getIdentifier().trim();
        if (identifier.isBlank() || request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new BizException(AuthErrorCode.INVALID_PARAM);
        }

        boolean ok = resetType == RegisterType.PHONE
                ? verificationCodeService.verifySmsCode(identifier, "reset", request.getCode())
                : verificationCodeService.verifyEmailCode(identifier, "reset", request.getCode());
        if (!ok) {
            throw new BizException(AuthErrorCode.CODE_ERROR);
        }

        String normalizedIdentifier = resetType == RegisterType.EMAIL ? identifier.toLowerCase() : identifier;
        userRemoteGateway.resetPassword(resetType.getCode(), normalizedIdentifier, request.getNewPassword(), request.getEstabId());
    }

    /**
     * 注册
     *
     * @param request  注册请求
     * @param context  登录上下文
     * @return         注册结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterRequest request, LoginContext context) {
        authSecurityService.checkRegister(request.getIdentifier(), context);

        RegisterType registerType = requireRegisterType(request.getRegisterType());
        validateRegisterCredential(registerType, request);

        Long estabId = userRemoteGateway.resolveEstabId(request.getEstabId(), request.getEstabCode());
        UserRegisterResult registerResult = userRemoteGateway.register(buildRegisterCommand(request, registerType, estabId));
        bindDefaultRole(registerResult.getUserId(), registerResult.getEstabId());
        return registerResult.getUserId();
    }

    /**
     * 登录
     *
     * @param request  登录请求
     * @param context  登录上下文
     * @return         登录结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request, LoginContext context) {
        authSecurityService.checkLogin(request.getIdentifier(), context);

        LoginType loginType = requireLoginType(request.getLoginType());
        Long estabId = userRemoteGateway.resolveEstabId(request.getEstabId(), request.getEstabCode());
        AuthPolicy policy = resolveAuthPolicy(estabId);
        assertLoginAllowed(policy, loginType);

        UserAuthSubjectDTO subject = resolveLoginSubject(loginType, request, estabId, context);
        ensureSubjectActive(subject);
        authenticateOrThrow(loginType, request, subject, estabId, context, policy);

        userRemoteGateway.markLoginSuccess(subject.getUserId(), subject.getIdentityId(), context.getIp());

        LoginLogEvent successEvent = LoginLogEvent.success(subject.getUserId(), estabId, subject.getIdentityId(), loginType, context.getSource(), context);
        return buildLoginResponse(subject, estabId, context, successEvent);
    }

    /**
     * 登出
     */
    @Override
    public void logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    /**
     * 获取注册类型
     *
     * @param registerTypeCode  注册类型代码
     * @return                  注册类型
     */
    private RegisterType requireRegisterType(Integer registerTypeCode) {
        RegisterType registerType = RegisterType.of(registerTypeCode);
        if (registerType == null) {
            throw new BizException(AuthErrorCode.REGISTER_TYPE_NOT_SUPPORTED);
        }
        return registerType;
    }

    /**
     * 获取登录类型
     *
     * @param loginTypeCode  登录类型代码
     * @return               登录类型
     */
    private LoginType requireLoginType(Integer loginTypeCode) {
        LoginType loginType = LoginType.of(loginTypeCode);
        if (loginType == null) {
            throw new BizException(AuthErrorCode.LOGIN_TYPE_NOT_SUPPORTED);
        }
        return loginType;
    }

    /**
     * 验证注册凭证
     *
     * @param registerType  注册类型
     * @param request       注册请求
     */
    private void validateRegisterCredential(RegisterType registerType, RegisterRequest request) {
        if (registerType == RegisterType.USERNAME && (request.getPassword() == null || request.getPassword().isBlank())) {
            throw new BizException(AuthErrorCode.INVALID_PARAM);
        }

        if (registerType == RegisterType.PHONE) {
            boolean ok = verificationCodeService.verifySmsCode(request.getIdentifier(), "register", request.getCode());
            if (!ok) {
                throw new BizException(AuthErrorCode.CODE_ERROR);
            }
        } else if (registerType == RegisterType.EMAIL) {
            boolean ok = verificationCodeService.verifyEmailCode(request.getIdentifier(), "register", request.getCode());
            if (!ok) {
                throw new BizException(AuthErrorCode.CODE_ERROR);
            }
        }
    }

    /**
     * 构建注册命令
     *
     * @param request       注册请求
     * @param registerType  注册类型
     * @param estabId       机构ID
     * @return              注册命令
     */
    private UserRegisterCommand buildRegisterCommand(RegisterRequest request, RegisterType registerType, Long estabId) {
        UserRegisterCommand command = new UserRegisterCommand();
        command.setRegisterType(registerType.getCode());
        command.setIdentifier(request.getIdentifier());
        command.setPassword(request.getPassword());
        command.setDisplayName(request.getDisplayName());
        command.setNickname(request.getNickname());
        command.setAvatarUrl(request.getAvatarUrl());
        command.setEstabId(estabId);
        command.setEstabCode(request.getEstabCode());
        command.setEstabName(request.getEstabName());
        command.setCreateEstab(request.getCreateEstab() != null ? request.getCreateEstab() : authProperties.isDefaultCreateEstab());
        command.setUserType(request.getUserType());
        return command;
    }

    /**
     * 解析登录主体
     *
     * @param loginType  登录类型
     * @param request    登录请求
     * @param estabId    机构ID
     * @param context    登录上下文
     * @return           登录主体
     */
    private UserAuthSubjectDTO resolveLoginSubject(LoginType loginType, LoginRequest request, Long estabId, LoginContext context) {
        Integer identityType = loginType.getIdentityType().getCode();
        UserAuthSubjectDTO subject = userRemoteGateway.queryAuthSubject(identityType, request.getIdentifier(), estabId);
        if (subject == null && loginType == LoginType.EMAIL_CODE) {
            subject = userRemoteGateway.queryAuthSubject(LoginType.EMAIL_PASSWORD.getIdentityType().getCode(), request.getIdentifier(), estabId);
        }
        if (subject == null && loginType == LoginType.PHONE_SMS && authProperties.isAutoRegisterOnSmsLogin()) {
            subject = autoRegisterAndLoadSubject(request, estabId);
        }
        if (subject == null) {
            loginLogService.recordLoginLog(LoginLogEvent.failure(null, estabId, null, loginType,
                    context.getSource(), AuthErrorCode.IDENTITY_NOT_FOUND.getMessage(), context));
            throw new BizException(AuthErrorCode.IDENTITY_NOT_FOUND);
        }
        return subject;
    }

    /**
     * 自动注册并加载登录主体
     *
     * @param request  登录请求
     * @param estabId  机构ID
     * @return         登录主体
     */
    private UserAuthSubjectDTO autoRegisterAndLoadSubject(LoginRequest request, Long estabId) {
        UserRegisterCommand registerCommand = new UserRegisterCommand();
        registerCommand.setRegisterType(RegisterType.PHONE.getCode());
        registerCommand.setIdentifier(request.getIdentifier());
        registerCommand.setEstabId(estabId);
        registerCommand.setCreateEstab(false);
        UserRegisterResult result = userRemoteGateway.register(registerCommand);
        bindDefaultRole(result.getUserId(), result.getEstabId());
        return userRemoteGateway.queryAuthSubject(LoginType.PHONE_SMS.getIdentityType().getCode(), request.getIdentifier(), estabId);
    }

    /**
     * 确保登录主体活跃
     *
     * @param subject  登录主体
     */
    private void ensureSubjectActive(UserAuthSubjectDTO subject) {
        if (subject.getIdentityStatus() != null && subject.getIdentityStatus() != 1) {
            throw new BizException(AuthErrorCode.IDENTITY_DISABLED);
        }
        if (subject.getUserStatus() == null || !Objects.equals(subject.getUserStatus(), UserStatus.ENABLED.getCode())) {
            throw new BizException(AuthErrorCode.USER_DISABLED);
        }
        if (subject.getLockUntil() != null && subject.getLockUntil().isAfter(LocalDateTime.now())) {
            throw new BizException(AuthErrorCode.USER_LOCKED);
        }
    }

    /**
     * 认证或抛出异常
     *
     * @param loginType  登录类型
     * @param request    登录请求
     * @param subject    登录主体
     * @param estabId    机构ID
     * @param context    登录上下文
     * @param policy     认证策略
     */
    private void authenticateOrThrow(LoginType loginType, LoginRequest request, UserAuthSubjectDTO subject, Long estabId, LoginContext context, AuthPolicy policy) {
        boolean authenticated;
        try {
            authenticated = authenticate(loginType, request, subject);
        } catch (BizException ex) {
            loginLogService.recordLoginLog(LoginLogEvent.failure(subject.getUserId(), estabId, subject.getIdentityId(),
                    loginType, context.getSource(), ex.getMessage(), context));
            throw ex;
        }

        if (!authenticated) {
            handleLoginFailure(subject, policy, context, loginType);
            throw new BizException(AuthErrorCode.PASSWORD_ERROR);
        }
    }

    /**
     * 认证
     *
     * @param loginType  登录类型
     * @param request    登录请求
     * @param subject    登录主体
     * @return           是否认证成功
     */
    private boolean authenticate(LoginType loginType, LoginRequest request, UserAuthSubjectDTO subject) {
        switch (loginType) {
            case USERNAME_PASSWORD, EMAIL_PASSWORD -> {
                if (request.getPassword() == null || request.getPassword().isBlank()) {
                    return false;
                }
                if (subject.getCredential() == null || subject.getCredential().isBlank()) {
                    return false;
                }
                return passwordEncoder.matches(request.getPassword(), subject.getCredential());
            }
            case PHONE_SMS -> {
                boolean ok = verificationCodeService.verifySmsCode(request.getIdentifier(), "login", request.getCode());
                if (!ok) {
                    throw new BizException(AuthErrorCode.CODE_ERROR);
                }
                return true;
            }
            case EMAIL_CODE -> {
                boolean ok = verificationCodeService.verifyEmailCode(request.getIdentifier(), "login", request.getCode());
                if (!ok) {
                    throw new BizException(AuthErrorCode.CODE_ERROR);
                }
                return true;
            }
            default -> throw new BizException(AuthErrorCode.LOGIN_TYPE_NOT_SUPPORTED);
        }
    }

    /**
     * 处理登录失败
     *
     * @param subject    登录主体
     * @param policy     认证策略
     * @param context    登录上下文
     * @param loginType  登录类型
     */
    private void handleLoginFailure(UserAuthSubjectDTO subject, AuthPolicy policy, LoginContext context, LoginType loginType) {
        int threshold = policy.loginFailThreshold == null ? authProperties.getLoginFailThreshold() : policy.loginFailThreshold;
        int lockMinutes = policy.lockMinutes == null ? authProperties.getLockMinutes() : policy.lockMinutes;
        userRemoteGateway.markLoginFailure(subject.getUserId(), threshold, lockMinutes);
        loginLogService.recordLoginLog(LoginLogEvent.failure(subject.getUserId(), subject.getPrimaryEstabId(), subject.getIdentityId(), loginType, context.getSource(), "Invalid credentials", context));
    }

    /**
     * 构建登录响应
     *
     * @param subject    登录主体
     * @param estabId    机构ID
     * @param context    登录上下文
     * @param logEvent   登录日志事件
     * @return           登录响应
     */
    private LoginResponse buildLoginResponse(UserAuthSubjectDTO subject, Long estabId, LoginContext context, LoginLogEvent logEvent) {
        LoginUser loginUser = buildLoginUser(subject, estabId);
        SaLoginParameter loginModel = new SaLoginParameter()
                .setTimeout(authProperties.getTokenTimeoutSeconds())
                .setActiveTimeout(authProperties.getActiveTimeoutSeconds())
                .setDeviceType(context.getSource() == null ? null : context.getSource().name());

        if (logEvent != null) {
            LoginLogContextHolder.set(logEvent);
        }

        try {
            StpUtil.login(subject.getUserId(), loginModel);
        } finally {
            LoginLogContextHolder.clear();
        }

        LoginUserHelper.setLoginUser(loginUser);

        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setTokenName(StpUtil.getTokenName());
        tokenInfo.setTokenValue(StpUtil.getTokenValue());
        tokenInfo.setTokenTimeout(StpUtil.getTokenTimeout());
        tokenInfo.setActiveTimeout(StpUtil.getTokenActiveTimeout());
        tokenInfo.setLoginId(subject.getUserId());

        LoginResponse response = new LoginResponse();
        response.setToken(tokenInfo);
        response.setLoginUser(loginUser);
        return response;
    }

    /**
     * 构建登录用户
     *
     * @param subject    登录主体
     * @param estabId    机构ID
     * @return           登录用户
     */
    private LoginUser buildLoginUser(UserAuthSubjectDTO subject, Long estabId) {
        Long resolvedEstabId = estabId != null ? estabId : subject.getPrimaryEstabId();
        List<String> roleCodes = resolvedEstabId == null
                ? Collections.emptyList()
                : authRbacMapper.findRoleCodes(subject.getUserId(), resolvedEstabId);
        if (roleCodes == null) {
            roleCodes = Collections.emptyList();
        }
        List<String> permissionCodes = resolvedEstabId == null
                ? Collections.emptyList()
                : authRbacMapper.findPermissionKeys(subject.getUserId(), resolvedEstabId);
        if (permissionCodes == null) {
            permissionCodes = Collections.emptyList();
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(subject.getUserId());
        loginUser.setUserCode(subject.getUserCode());
        loginUser.setUsername(subject.getUsername());
        loginUser.setDisplayName(subject.getDisplayName());
        loginUser.setNickname(subject.getNickname());
        loginUser.setAvatarUrl(subject.getAvatarUrl());
        loginUser.setUserType(UserType.of(subject.getUserType()));
        loginUser.setStatus(UserStatus.of(subject.getUserStatus()));
        loginUser.setPrimaryEstabId(subject.getPrimaryEstabId());
        loginUser.setEstabId(resolvedEstabId);
        loginUser.setTeamId(subject.getTeamId());
        loginUser.setEstabAdmin(Boolean.TRUE.equals(subject.getEstabAdmin()));
        loginUser.setRoleCodes(roleCodes);
        loginUser.setPermissionCodes(permissionCodes);
        loginUser.setLoginTime(LocalDateTime.now());
        return loginUser;
    }

    /**
     * 绑定默认角色
     *
     * @param userId     用户ID
     * @param estabId    机构ID
     */
    private void bindDefaultRole(Long userId, Long estabId) {
        if (userId == null || estabId == null) {
            return;
        }

        ScrRole role = scrRoleMapper.selectOne(
                Wrappers.lambdaQuery(ScrRole.class)
                        .eq(ScrRole::getRoleCode, authProperties.getDefaultRoleCode())
                        .eq(ScrRole::getEstabId, estabId)
                        .eq(ScrRole::getDeleted, 0)
                        .last(LIMIT_ONE)
        );

        if (role == null) {
            return;
        }

        ScrRoleUser roleUser = new ScrRoleUser();
        roleUser.setRoleId(role.getId());
        roleUser.setUserId(userId);
        roleUser.setEstabId(estabId);
        roleUser.setStatus(1);
        roleUser.setGrantedTime(LocalDateTime.now());
        scrRoleUserMapper.insert(roleUser);
    }

    /**
     * 解析认证策略
     *
     * @param estabId    机构ID
     * @return           认证策略
     */
    private AuthPolicy resolveAuthPolicy(Long estabId) {
        if (estabId == null) {
            return AuthPolicy.fromDefault(authProperties);
        }

        DefEstabAuthPolicy policy = defEstabAuthPolicyMapper.selectOne(
                Wrappers.lambdaQuery(DefEstabAuthPolicy.class)
                        .eq(DefEstabAuthPolicy::getEstabId, estabId)
                        .eq(DefEstabAuthPolicy::getDeleted, 0)
                        .last(LIMIT_ONE)
        );
        if (policy == null) {
            return AuthPolicy.fromDefault(authProperties);
        }

        AuthPolicy authPolicy = new AuthPolicy();
        authPolicy.passwordLoginEnabled = policy.getPasswordLoginEnabled();
        authPolicy.smsLoginEnabled = policy.getSmsLoginEnabled();
        authPolicy.emailLoginEnabled = policy.getEmailLoginEnabled();
        authPolicy.wechatLoginEnabled = policy.getWechatLoginEnabled();
        authPolicy.loginFailThreshold = policy.getLoginFailThreshold();
        authPolicy.lockMinutes = policy.getLockMinutes();
        return authPolicy;
    }

    /**
     * 确保登录允许
     *
     * @param policy     认证策略
     * @param loginType  登录类型
     */
    private void assertLoginAllowed(AuthPolicy policy, LoginType loginType) {
        if (policy == null || loginType == null) {
            return;
        }

        switch (loginType) {
            case USERNAME_PASSWORD -> ensureLoginAllowed(policy.passwordLoginEnabled);
            case PHONE_SMS -> ensureLoginAllowed(policy.smsLoginEnabled);
            case EMAIL_PASSWORD, EMAIL_CODE -> ensureLoginAllowed(policy.emailLoginEnabled);
            case WECHAT_QR -> ensureLoginAllowed(policy.wechatLoginEnabled);
            default -> log.warn("Unknown login type: {}", loginType);
        }
    }

    /**
     * 确保登录允许
     *
     * @param enabledFlag  启用标志
     */
    private void ensureLoginAllowed(Integer enabledFlag) {
        if (isForbidden(enabledFlag)) {
            throw new BizException(AuthErrorCode.ESTAB_LOGIN_FORBIDDEN);
        }
    }

    /**
     * 判断是否禁止
     *
     * @param flag   标志
     * @return       是否禁止
     */
    private boolean isForbidden(Integer flag) {
        return flag != null && flag == 0;
    }

    /**
     * 认证策略
     */
    private static class AuthPolicy {

        /**
         * 密码登录启用标志
         */
        private Integer passwordLoginEnabled;

        /**
         * 短信登录启用标志
         */
        private Integer smsLoginEnabled;

        /**
         * 邮箱登录启用标志
         */
        private Integer emailLoginEnabled;

        /**
         * 微信登录启用标志
         */
        private Integer wechatLoginEnabled;

        /**
         * 登录失败阈值
         */
        private Integer loginFailThreshold;

        /**
         * 锁定分钟数
         */
        private Integer lockMinutes;

        /**
         * 从默认配置创建认证策略
         *
         * @param properties   认证属性
         * @return             认证策略
         */
        private static AuthPolicy fromDefault(AuthProperties properties) {
            AuthPolicy policy = new AuthPolicy();
            policy.passwordLoginEnabled = 1;
            policy.smsLoginEnabled = 1;
            policy.emailLoginEnabled = 1;
            policy.wechatLoginEnabled = 1;
            policy.loginFailThreshold = properties.getLoginFailThreshold();
            policy.lockMinutes = properties.getLockMinutes();
            return policy;
        }
    }
}
