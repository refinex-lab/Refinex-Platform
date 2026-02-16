package cn.refinex.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.refinex.api.user.enums.UserStatus;
import cn.refinex.api.user.enums.UserType;
import cn.refinex.api.user.model.context.LoginUser;
import cn.refinex.auth.api.dto.LoginRequest;
import cn.refinex.auth.api.dto.RegisterRequest;
import cn.refinex.auth.api.dto.SmsSendRequest;
import cn.refinex.auth.api.vo.LoginResponse;
import cn.refinex.auth.api.vo.TokenInfo;
import cn.refinex.auth.config.AuthProperties;
import cn.refinex.auth.domain.entity.*;
import cn.refinex.auth.domain.enums.IdentityType;
import cn.refinex.auth.domain.enums.LoginType;
import cn.refinex.auth.domain.enums.RegisterType;
import cn.refinex.auth.domain.error.AuthErrorCode;
import cn.refinex.auth.domain.model.LoginContext;
import cn.refinex.auth.domain.model.LoginLogContextHolder;
import cn.refinex.auth.domain.model.LoginLogEvent;
import cn.refinex.auth.infrastructure.mapper.*;
import cn.refinex.auth.service.AuthSecurityService;
import cn.refinex.auth.service.AuthService;
import cn.refinex.auth.service.LoginLogService;
import cn.refinex.auth.service.VerificationCodeService;
import cn.refinex.base.exception.BizException;
import cn.refinex.satoken.helper.LoginUserHelper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
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
    private final ObjectProvider<AuthService> authServiceProvider;
    private final DefUserMapper defUserMapper;
    private final DefUserIdentityMapper defUserIdentityMapper;
    private final DefEstabMapper defEstabMapper;
    private final DefEstabUserMapper defEstabUserMapper;
    private final DefTeamUserMapper defTeamUserMapper;
    private final DefEstabAuthPolicyMapper defEstabAuthPolicyMapper;
    private final ScrRoleMapper scrRoleMapper;
    private final ScrRoleUserMapper scrRoleUserMapper;
    private final ScrSystemMapper scrSystemMapper;
    private final AuthRbacMapper authRbacMapper;

    /**
     * 发送验证码
     *
     * @param request  请求
     * @param context  登录上下文
     */
    @Override
    public void sendSmsCode(SmsSendRequest request, LoginContext context) {
        authSecurityService.checkSmsSend(request.getPhone(), context);

        if (request.getEstabId() != null) {
            AuthPolicy policy = resolveAuthPolicy(request.getEstabId());
            if (policy.smsLoginEnabled != null && policy.smsLoginEnabled == 0) {
                throw new BizException(AuthErrorCode.ESTAB_LOGIN_FORBIDDEN);
            }
        }

        verificationCodeService.sendSmsCode(request.getPhone(), request.getScene());
    }

    /**
     * 注册
     *
     * @param request  请求
     * @param context  登录上下文
     * @return         注册的用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterRequest request, LoginContext context) {
        authSecurityService.checkRegister(request.getIdentifier(), context);

        RegisterType registerType = requireRegisterType(request);
        validateRegisterCredential(registerType, request);
        IdentityType identityType = requireIdentityType(registerType);
        ensureIdentityNotExists(identityType, request.getIdentifier());

        Long estabId = resolveExistingEstabId(request.getEstabId(), request.getEstabCode());
        DefUser user = createUser(registerType, request);
        estabId = ensureEstabAndLink(user, request, estabId);
        createUserIdentity(user, request, registerType, identityType);
        bindDefaultRole(user.getId(), estabId);
        return user.getId();
    }

    /**
     * 登录
     *
     * @param request  请求
     * @param context  登录上下文
     * @return         登录响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request, LoginContext context) {
        authSecurityService.checkLogin(request.getIdentifier(), context);

        LoginType loginType = requireLoginType(request);
        Long estabId = resolveExistingEstabId(request.getEstabId(), request.getEstabCode());
        AuthPolicy policy = resolveAuthPolicy(estabId);
        assertLoginAllowed(policy, loginType);

        DefUserIdentity identity = resolveLoginIdentity(loginType, request, estabId, context);
        ensureIdentityActive(identity);
        DefUser user = loadAndValidateUser(identity);
        authenticateOrThrow(loginType, request, identity, user, estabId, context, policy);

        LoginLogEvent successEvent = LoginLogEvent.success(user.getId(), estabId, identity.getId(), loginType, context.getSource(), context);
        return buildLoginResponse(user, estabId, context, successEvent);
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
     * 将注册类型映射到身份类型
     *
     * @param registerType  注册类型
     * @return              身份类型
     */
    private IdentityType mapRegisterIdentityType(RegisterType registerType) {
        return switch (registerType) {
            case USERNAME -> IdentityType.USERNAME_PASSWORD;
            case PHONE -> IdentityType.PHONE_SMS;
            case EMAIL -> IdentityType.EMAIL_PASSWORD;
        };
    }

    /**
     * 获取注册类型
     *
     * @param request  注册请求
     * @return         注册类型
     */
    private RegisterType requireRegisterType(RegisterRequest request) {
        RegisterType registerType = RegisterType.of(request.getRegisterType());
        if (registerType == null) {
            throw new BizException(AuthErrorCode.REGISTER_TYPE_NOT_SUPPORTED);
        }
        return registerType;
    }

    /**
     * 验证注册凭证
     *
     * @param registerType  注册类型
     * @param request       注册请求
     */
    private void validateRegisterCredential(RegisterType registerType, RegisterRequest request) {
        if ((registerType == RegisterType.USERNAME || registerType == RegisterType.EMAIL) && (request.getPassword() == null || request.getPassword().isBlank())) {
            throw new BizException(AuthErrorCode.INVALID_PARAM);
        }

        if (registerType == RegisterType.PHONE) {
            boolean ok = verificationCodeService.verifySmsCode(request.getIdentifier(), "register", request.getCode());
            if (!ok) {
                throw new BizException(AuthErrorCode.CODE_ERROR);
            }
        }
    }

    /**
     * 获取注册的用户身份类型
     *
     * @param registerType  注册类型
     * @return              身份类型
     */
    private IdentityType requireIdentityType(RegisterType registerType) {
        IdentityType identityType = mapRegisterIdentityType(registerType);
        if (identityType == null) {
            throw new BizException(AuthErrorCode.REGISTER_TYPE_NOT_SUPPORTED);
        }

        return identityType;
    }

    /**
     * 确保用户身份不存在
     *
     * @param identityType  身份类型
     * @param identifier    身份标识
     */
    private void ensureIdentityNotExists(IdentityType identityType, String identifier) {
        Long existing = defUserIdentityMapper.selectCount(
                Wrappers.lambdaQuery(DefUserIdentity.class)
                        .eq(DefUserIdentity::getIdentityType, identityType.getCode())
                        .eq(DefUserIdentity::getIdentifier, identifier)
                        .eq(DefUserIdentity::getDeleted, 0)
        );

        if (existing != null && existing > 0) {
            throw new BizException(AuthErrorCode.DUPLICATE_IDENTITY);
        }
    }

    /**
     * 创建用户
     *
     * @param registerType  注册类型
     * @param request       注册请求
     * @return              用户
     */
    private DefUser createUser(RegisterType registerType, RegisterRequest request) {
        DefUser user = new DefUser();
        user.setUserCode(generateUserCode());
        user.setUsername(registerType == RegisterType.USERNAME ? request.getIdentifier() : null);
        user.setDisplayName(request.getDisplayName() == null ? request.getIdentifier() : request.getDisplayName());
        user.setNickname(request.getNickname());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setUserType(request.getUserType() != null ? request.getUserType() : UserType.TENANT.getCode());
        user.setStatus(UserStatus.ENABLED.getCode());
        user.setLoginFailCount(0);

        if (registerType == RegisterType.PHONE) {
            user.setPrimaryPhone(request.getIdentifier());
            user.setPhoneVerified(1);
        } else if (registerType == RegisterType.EMAIL) {
            user.setPrimaryEmail(request.getIdentifier());
            user.setEmailVerified(0);
        }

        defUserMapper.insert(user);
        return user;
    }

    /**
     * 确保用户所属组织存在并关联
     *
     * @param user       用户
     * @param request    注册请求
     * @param estabId    组织ID
     * @return           组织ID
     */
    private Long ensureEstabAndLink(DefUser user, RegisterRequest request, Long estabId) {
        Long resolvedEstabId = maybeCreateEstab(request, user, estabId);
        if (resolvedEstabId != null) {
            user.setPrimaryEstabId(resolvedEstabId);
            defUserMapper.updateById(user);
            createEstabUserRelation(user, request, resolvedEstabId);
        }

        return resolvedEstabId;
    }

    /**
     * 可能创建组织
     *
     * @param request  注册请求
     * @param user     用户
     * @param estabId  组织ID
     * @return         组织ID
     */
    private Long maybeCreateEstab(RegisterRequest request, DefUser user, Long estabId) {
        if (estabId != null) {
            return estabId;
        }
        if (!shouldCreateEstab(request)) {
            return null;
        }

        DefEstab estab = new DefEstab();
        estab.setEstabCode(generateEstabCode());
        estab.setEstabName(request.getEstabName() == null ? "默认组织" : request.getEstabName());
        estab.setEstabType(1);
        estab.setStatus(1);
        estab.setOwnerUserId(user.getId());
        defEstabMapper.insert(estab);
        return estab.getId();
    }

    /**
     * 是否应该创建组织
     *
     * @param request  注册请求
     * @return         是否应该创建组织
     */
    private boolean shouldCreateEstab(RegisterRequest request) {
        return request.getCreateEstab() != null
                ? request.getCreateEstab()
                : authProperties.isDefaultCreateEstab();
    }

    /**
     * 创建组织用户关系
     *
     * @param user       用户
     * @param request    注册请求
     * @param estabId    组织ID
     */
    private void createEstabUserRelation(DefUser user, RegisterRequest request, Long estabId) {
        DefEstabUser estabUser = new DefEstabUser();
        estabUser.setEstabId(estabId);
        estabUser.setUserId(user.getId());
        estabUser.setIsAdmin(Objects.equals(request.getCreateEstab(), Boolean.TRUE) ? 1 : 0);
        estabUser.setStatus(1);
        estabUser.setJoinTime(LocalDateTime.now());
        defEstabUserMapper.insert(estabUser);
    }

    /**
     * 创建用户身份
     *
     * @param user             用户
     * @param request          注册请求
     * @param registerType     注册类型
     * @param identityType     身份类型
     */
    private void createUserIdentity(DefUser user, RegisterRequest request, RegisterType registerType, IdentityType identityType) {
        DefUserIdentity identity = new DefUserIdentity();
        identity.setUserId(user.getId());
        identity.setIdentityType(identityType.getCode());
        identity.setIdentifier(request.getIdentifier());
        identity.setIssuer("");
        identity.setStatus(1);
        identity.setIsPrimary(1);
        LocalDateTime now = LocalDateTime.now();
        identity.setVerified(registerType == RegisterType.PHONE ? 1 : 0);
        identity.setVerifiedAt(registerType == RegisterType.PHONE ? now : null);
        identity.setBindTime(now);
        if (registerType == RegisterType.USERNAME || registerType == RegisterType.EMAIL) {
            identity.setCredential(passwordEncoder.encode(request.getPassword()));
            identity.setCredentialAlg("bcrypt");
        }
        defUserIdentityMapper.insert(identity);
    }

    /**
     * 根据登录类型和标识符查找身份
     *
     * @param loginType     登录类型
     * @param identifier    标识符
     * @return              身份
     */
    private DefUserIdentity findIdentity(LoginType loginType, String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return null;
        }

        return defUserIdentityMapper.selectOne(
                Wrappers.lambdaQuery(DefUserIdentity.class)
                        .eq(DefUserIdentity::getIdentityType, loginType.getIdentityType().getCode())
                        .eq(DefUserIdentity::getIdentifier, identifier)
                        .eq(DefUserIdentity::getDeleted, 0)
        );
    }

    /**
     * 要求登录类型
     *
     * @param request  登录请求
     * @return         登录类型
     */
    private LoginType requireLoginType(LoginRequest request) {
        LoginType loginType = LoginType.of(request.getLoginType());
        if (loginType == null) {
            throw new BizException(AuthErrorCode.LOGIN_TYPE_NOT_SUPPORTED);
        }
        return loginType;
    }

    /**
     * 解析登录身份
     *
     * @param loginType     登录类型
     * @param request       登录请求
     * @param estabId       组织ID
     * @param context       登录上下文
     * @return              登录身份
     */
    private DefUserIdentity resolveLoginIdentity(LoginType loginType, LoginRequest request, Long estabId, LoginContext context) {
        DefUserIdentity identity = findIdentity(loginType, request.getIdentifier());
        if (identity == null && loginType == LoginType.PHONE_SMS && authProperties.isAutoRegisterOnSmsLogin()) {
            identity = autoRegisterAndLoadIdentity(loginType, request, estabId, context);
        }
        if (identity == null) {
            loginLogService.recordLoginLog(LoginLogEvent.failure(null, estabId, null, loginType,
                    context.getSource(), AuthErrorCode.IDENTITY_NOT_FOUND.getMessage(), context));
            throw new BizException(AuthErrorCode.IDENTITY_NOT_FOUND);
        }
        return identity;
    }

    /**
     * 自动注册并加载身份
     *
     * @param loginType     登录类型
     * @param request       登录请求
     * @param estabId       组织ID
     * @param context       登录上下文
     * @return              身份
     */
    private DefUserIdentity autoRegisterAndLoadIdentity(LoginType loginType, LoginRequest request, Long estabId, LoginContext context) {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setRegisterType(RegisterType.PHONE.getCode());
        registerRequest.setIdentifier(request.getIdentifier());
        registerRequest.setCode(request.getCode());
        registerRequest.setEstabId(estabId);
        registerRequest.setCreateEstab(false);
        Long userId = authServiceProvider.getObject().register(registerRequest, context);
        return defUserIdentityMapper.selectOne(
                Wrappers.lambdaQuery(DefUserIdentity.class)
                        .eq(DefUserIdentity::getUserId, userId)
                        .eq(DefUserIdentity::getIdentityType, loginType.getIdentityType().getCode())
                        .eq(DefUserIdentity::getDeleted, 0)
        );
    }

    /**
     * 确保身份活跃
     *
     * @param identity   身份
     */
    private void ensureIdentityActive(DefUserIdentity identity) {
        if (identity.getStatus() != null && identity.getStatus() != 1) {
            throw new BizException(AuthErrorCode.IDENTITY_DISABLED);
        }
    }

    /**
     * 加载并验证用户
     *
     * @param identity   身份
     * @return           用户
     */
    private DefUser loadAndValidateUser(DefUserIdentity identity) {
        DefUser user = defUserMapper.selectById(identity.getUserId());
        if (user == null || user.getDeleted() != null && user.getDeleted() == 1) {
            throw new BizException(AuthErrorCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == null || !Objects.equals(user.getStatus(), UserStatus.ENABLED.getCode())) {
            throw new BizException(AuthErrorCode.USER_DISABLED);
        }
        if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
            throw new BizException(AuthErrorCode.USER_LOCKED);
        }
        return user;
    }

    /**
     * 认证或抛出异常
     *
     * @param loginType     登录类型
     * @param request       登录请求
     * @param identity      身份
     * @param user          用户
     * @param estabId       组织ID
     * @param context       登录上下文
     * @param policy        认证策略
     */
    private void authenticateOrThrow(LoginType loginType, LoginRequest request, DefUserIdentity identity, DefUser user, Long estabId, LoginContext context, AuthPolicy policy) {
        boolean authenticated;
        try {
            authenticated = authenticate(loginType, request, identity);
        } catch (BizException ex) {
            loginLogService.recordLoginLog(LoginLogEvent.failure(user.getId(), estabId, identity.getId(), loginType, context.getSource(), ex.getMessage(), context));
            throw ex;
        }
        if (!authenticated) {
            handleLoginFailure(user, policy, context, loginType);
            throw new BizException(AuthErrorCode.PASSWORD_ERROR);
        }
        resetLoginFailure(user);
        updateLoginSuccess(user, identity, context);
    }

    /**
     * 构建登录响应
     *
     * @param user       用户
     * @param estabId    组织ID
     * @param context    登录上下文
     * @return           登录响应
     */
    private LoginResponse buildLoginResponse(DefUser user, Long estabId, LoginContext context, LoginLogEvent logEvent) {
        LoginUser loginUser = buildLoginUser(user, estabId);
        SaLoginParameter loginModel = new SaLoginParameter()
                .setTimeout(authProperties.getTokenTimeoutSeconds())
                .setActiveTimeout(authProperties.getActiveTimeoutSeconds())
                .setDeviceType(context.getSource() == null ? null : context.getSource().name());
        if (logEvent != null) {
            LoginLogContextHolder.set(logEvent);
        }
        try {
            StpUtil.login(user.getId(), loginModel);
        } finally {
            LoginLogContextHolder.clear();
        }
        LoginUserHelper.setLoginUser(loginUser);

        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setTokenName(StpUtil.getTokenName());
        tokenInfo.setTokenValue(StpUtil.getTokenValue());
        tokenInfo.setTokenTimeout(StpUtil.getTokenTimeout());
        tokenInfo.setActiveTimeout(StpUtil.getTokenActiveTimeout());
        tokenInfo.setLoginId(user.getId());

        LoginResponse response = new LoginResponse();
        response.setToken(tokenInfo);
        response.setLoginUser(loginUser);
        return response;
    }

    /**
     * 认证
     *
     * @param loginType     登录类型
     * @param request       登录请求
     * @param identity      身份
     * @return              是否认证成功
     */
    private boolean authenticate(LoginType loginType, LoginRequest request, DefUserIdentity identity) {
        switch (loginType) {
            case USERNAME_PASSWORD, EMAIL_PASSWORD -> {
                if (request.getPassword() == null || request.getPassword().isBlank()) {
                    return false;
                }
                if (identity.getCredential() == null || identity.getCredential().isBlank()) {
                    return false;
                }
                return passwordEncoder.matches(request.getPassword(), identity.getCredential());
            }
            case PHONE_SMS -> {
                boolean ok = verificationCodeService.verifySmsCode(request.getIdentifier(), "login", request.getCode());
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
     * @param user       用户
     * @param policy     认证策略
     * @param context    登录上下文
     * @param loginType  登录类型
     */
    private void handleLoginFailure(DefUser user, AuthPolicy policy, LoginContext context, LoginType loginType) {
        int failCount = user.getLoginFailCount() == null ? 0 : user.getLoginFailCount();
        failCount += 1;
        user.setLoginFailCount(failCount);

        int threshold = policy.loginFailThreshold == null ? authProperties.getLoginFailThreshold() : policy.loginFailThreshold;
        int lockMinutes = policy.lockMinutes == null ? authProperties.getLockMinutes() : policy.lockMinutes;
        if (threshold > 0 && failCount >= threshold) {
            user.setLockUntil(LocalDateTime.now().plusMinutes(lockMinutes));
            user.setLoginFailCount(0);
        }

        defUserMapper.updateById(user);
        loginLogService.recordLoginLog(LoginLogEvent.failure(user.getId(), user.getPrimaryEstabId(), null, loginType, context.getSource(), "Invalid credentials", context));
    }

    /**
     * 重置登录失败次数
     *
     * @param user  用户
     */
    private void resetLoginFailure(DefUser user) {
        user.setLoginFailCount(0);
        defUserMapper.updateById(user);
    }

    /**
     * 更新登录成功信息
     *
     * @param user       用户
     * @param identity   身份
     * @param context    登录上下文
     */
    private void updateLoginSuccess(DefUser user, DefUserIdentity identity, LoginContext context) {
        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginTime(now);
        user.setLastLoginIp(context.getIp());
        defUserMapper.updateById(user);

        identity.setLastLoginTime(now);
        identity.setLastLoginIp(context.getIp());
        defUserIdentityMapper.updateById(identity);
    }

    /**
     * 构建登录用户
     *
     * @param user       用户
     * @param estabId    机构ID
     * @return           登录用户
     */
    private LoginUser buildLoginUser(DefUser user, Long estabId) {
        Long resolvedEstabId = estabId != null ? estabId : user.getPrimaryEstabId();
        Long teamId = defTeamUserMapper.selectFirstTeamId(user.getId());
        DefEstabUser estabUser = resolvedEstabId == null ? null : defEstabUserMapper.selectActive(user.getId(), resolvedEstabId);

        Long systemId = resolveSystemId(authProperties.getDefaultSystemCode());
        List<String> roleCodes = resolvedEstabId == null
                ? Collections.emptyList()
                : authRbacMapper.findRoleCodes(user.getId(), resolvedEstabId, systemId);
        if (roleCodes == null) {
            roleCodes = Collections.emptyList();
        }
        List<String> permissionCodes = resolvedEstabId == null
                ? Collections.emptyList()
                : authRbacMapper.findPermissionKeys(user.getId(), resolvedEstabId, systemId);
        if (permissionCodes == null) {
            permissionCodes = Collections.emptyList();
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUserCode(user.getUserCode());
        loginUser.setUsername(user.getUsername());
        loginUser.setDisplayName(user.getDisplayName());
        loginUser.setNickname(user.getNickname());
        loginUser.setAvatarUrl(user.getAvatarUrl());
        loginUser.setUserType(UserType.of(user.getUserType()));
        loginUser.setStatus(UserStatus.of(user.getStatus()));
        loginUser.setPrimaryEstabId(user.getPrimaryEstabId());
        loginUser.setEstabId(resolvedEstabId);
        loginUser.setTeamId(teamId);
        loginUser.setEstabAdmin(estabUser != null && estabUser.getIsAdmin() != null && estabUser.getIsAdmin() == 1);
        loginUser.setRoleCodes(roleCodes);
        loginUser.setPermissionCodes(permissionCodes);
        loginUser.setLoginTime(LocalDateTime.now());
        return loginUser;
    }

    /**
     * 绑定默认角色
     *
     * @param userId   用户ID
     * @param estabId  机构ID
     */
    private void bindDefaultRole(Long userId, Long estabId) {
        if (userId == null) {
            return;
        }
        if (estabId == null) {
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
     * 解析系统ID
     *
     * @param systemCode  系统代码
     * @return            系统ID
     */
    private Long resolveSystemId(String systemCode) {
        if (systemCode == null || systemCode.isBlank()) {
            return null;
        }

        ScrSystem system = scrSystemMapper.selectOne(
                Wrappers.lambdaQuery(ScrSystem.class)
                        .eq(ScrSystem::getSystemCode, systemCode)
                        .eq(ScrSystem::getDeleted, 0)
                        .last(LIMIT_ONE)
        );
        return system == null ? null : system.getId();
    }

    /**
     * 解析已存在的机构ID
     *
     * @param estabId     机构ID
     * @param estabCode   机构代码
     * @return            机构ID
     */
    private Long resolveExistingEstabId(Long estabId, String estabCode) {
        if (estabId != null) {
            return estabId;
        }

        if (estabCode != null && !estabCode.isBlank()) {
            DefEstab estab = defEstabMapper.selectOne(
                    Wrappers.lambdaQuery(DefEstab.class)
                            .eq(DefEstab::getEstabCode, estabCode)
                            .eq(DefEstab::getDeleted, 0)
                            .last(LIMIT_ONE)
            );

            if (estab == null) {
                throw new BizException(AuthErrorCode.ESTAB_NOT_FOUND);
            }

            return estab.getId();
        }
        return null;
    }

    /**
     * 解析认证策略
     *
     * @param estabId  机构ID
     * @return         认证策略
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
     * @param policy       认证策略
     * @param loginType    登录类型
     */
    private void assertLoginAllowed(AuthPolicy policy, LoginType loginType) {
        if (policy == null || loginType == null) {
            return;
        }
        
        switch (loginType) {
            case LoginType lt when lt == LoginType.USERNAME_PASSWORD && isForbidden(policy.passwordLoginEnabled)
                    -> throw new BizException(AuthErrorCode.ESTAB_LOGIN_FORBIDDEN);
            case LoginType lt when lt == LoginType.PHONE_SMS && isForbidden(policy.smsLoginEnabled)
                    -> throw new BizException(AuthErrorCode.ESTAB_LOGIN_FORBIDDEN);
            case LoginType lt when (lt == LoginType.EMAIL_PASSWORD || lt == LoginType.EMAIL_CODE)
                    && isForbidden(policy.emailLoginEnabled)
                    -> throw new BizException(AuthErrorCode.ESTAB_LOGIN_FORBIDDEN);
            case LoginType lt when lt == LoginType.WECHAT_QR && isForbidden(policy.wechatLoginEnabled)
                    -> throw new BizException(AuthErrorCode.ESTAB_LOGIN_FORBIDDEN);
            default -> log.debug("Login type {} is not forbidden", loginType);
        }
    }

    /**
     * 是否禁止
     *
     * @param flag  标志
     * @return      是否禁止
     */
    private boolean isForbidden(Integer flag) {
        return flag != null && flag == 0;
    }

    /**
     * 生成用户代码
     *
     * @return  用户代码
     */
    private String generateUserCode() {
        return "U" + System.currentTimeMillis();
    }

    /**
     * 生成机构代码
     *
     * @return  机构代码
     */
    private String generateEstabCode() {
        return "E" + System.currentTimeMillis();
    }

    /**
     * 认证策略
     */
    private static class AuthPolicy {
        /**
         * 密码登录是否启用
         */
        private Integer passwordLoginEnabled;

        /**
         * 短信登录是否启用
         */
        private Integer smsLoginEnabled;

        /**
         * 邮箱登录是否启用
         */
        private Integer emailLoginEnabled;

        /**
         * 微信登录是否启用
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
         * @param properties  认证属性
         * @return            认证策略
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
