package cn.refinex.user.application.service;

import cn.refinex.api.user.enums.UserStatus;
import cn.refinex.api.user.enums.UserType;
import cn.refinex.user.application.assembler.UserDomainAssembler;
import cn.refinex.user.application.command.QueryAuthSubjectCommand;
import cn.refinex.user.application.command.QueryUserInfoCommand;
import cn.refinex.user.application.command.RegisterUserCommand;
import cn.refinex.user.application.command.ResetPasswordCommand;
import cn.refinex.user.application.command.ResolveEstabCommand;
import cn.refinex.user.application.command.UpdateLoginFailureCommand;
import cn.refinex.user.application.command.UpdateLoginSuccessCommand;
import cn.refinex.user.application.dto.AuthSubjectDTO;
import cn.refinex.user.application.dto.RegisterUserResultDTO;
import cn.refinex.user.application.dto.UserInfoDTO;
import cn.refinex.user.domain.error.UserErrorCode;
import cn.refinex.user.domain.model.entity.UserAuthSubject;
import cn.refinex.user.domain.model.entity.UserEntity;
import cn.refinex.user.domain.model.entity.UserIdentityEntity;
import cn.refinex.user.domain.model.enums.IdentityType;
import cn.refinex.user.domain.model.enums.RegisterType;
import cn.refinex.user.domain.repository.UserRepository;
import cn.refinex.base.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户应用服务
 *
 * @author refinex
 */
@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private static final String DEFAULT_ESTAB_NAME = "默认组织";

    private final UserRepository userRepository;
    private final UserDomainAssembler userDomainAssembler;
    private final PasswordEncoder passwordEncoder;

    /**
     * 注册用户
     *
     * @param command 注册命令
     * @return 注册结果
     */
    @Transactional(rollbackFor = Exception.class)
    public RegisterUserResultDTO register(RegisterUserCommand command) {
        RegisterType registerType = requireRegisterType(command.getRegisterType());
        IdentityType identityType = mapIdentityType(registerType);
        ensureIdentityNotExists(identityType.getCode(), command.getIdentifier());

        Long estabId = resolveEstabIdInternal(command.getEstabId(), command.getEstabCode());
        UserEntity user = buildUser(registerType, command);
        user = userRepository.insertUser(user);

        if (estabId == null && Boolean.TRUE.equals(command.getCreateEstab())) {
            estabId = userRepository.insertEstab(generateEstabCode(), command.getEstabName() == null ? DEFAULT_ESTAB_NAME : command.getEstabName(), user.getId());
        }
        if (estabId != null) {
            userRepository.updateUserPrimaryEstab(user.getId(), estabId);
            userRepository.insertEstabUserRelation(estabId, user.getId(), Boolean.TRUE.equals(command.getCreateEstab()) ? 1 : 0, LocalDateTime.now());
        }

        UserIdentityEntity identity = buildIdentity(identityType.getCode(), registerType, command, user.getId());
        userRepository.insertIdentity(identity);

        RegisterUserResultDTO result = new RegisterUserResultDTO();
        result.setUserId(user.getId());
        result.setEstabId(estabId);
        return result;
    }

    /**
     * 解析组织ID
     *
     * @param command 解析命令
     * @return 组织ID
     */
    public Long resolveEstabId(ResolveEstabCommand command) {
        return resolveEstabIdInternal(command.getEstabId(), command.getEstabCode());
    }

    /**
     * 查询认证主体
     *
     * @param command 查询命令
     * @return 认证主体
     */
    public AuthSubjectDTO queryAuthSubject(QueryAuthSubjectCommand command) {
        if (command.getIdentityType() == null || command.getIdentifier() == null || command.getIdentifier().isBlank()) {
            throw new BizException(UserErrorCode.INVALID_PARAM);
        }

        UserIdentityEntity identity = userRepository.findIdentityByTypeAndIdentifier(command.getIdentityType(), command.getIdentifier());
        if (identity == null) {
            return null;
        }

        UserEntity user = userRepository.findUserById(identity.getUserId());
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            return null;
        }

        Long resolvedEstabId = command.getEstabId() != null ? command.getEstabId() : user.getPrimaryEstabId();
        if (resolvedEstabId != null && !userRepository.hasActiveEstabMembership(user.getId(), resolvedEstabId)) {
            return null;
        }
        UserAuthSubject subject = new UserAuthSubject();
        subject.setUser(user);
        subject.setIdentity(identity);
        subject.setTeamId(userRepository.findFirstTeamId(user.getId()));
        subject.setEstabAdmin(userRepository.isEstabAdmin(user.getId(), resolvedEstabId));
        return userDomainAssembler.toAuthSubjectDto(subject);
    }

    /**
     * 标记登录成功
     *
     * @param command 登录成功命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void markLoginSuccess(UpdateLoginSuccessCommand command) {
        userRepository.markLoginSuccess(command.getUserId(), command.getIdentityId(), command.getIp(), LocalDateTime.now());
    }

    /**
     * 标记登录失败
     *
     * @param command 登录失败命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void markLoginFailure(UpdateLoginFailureCommand command) {
        int failCount = userRepository.incrementLoginFailCount(command.getUserId());
        int threshold = command.getThreshold() == null ? 5 : command.getThreshold();
        int lockMinutes = command.getLockMinutes() == null ? 30 : command.getLockMinutes();
        if (threshold > 0 && failCount >= threshold) {
            userRepository.lockUser(command.getUserId(), LocalDateTime.now().plusMinutes(lockMinutes));
        }
    }

    /**
     * 重置密码（手机号验证码 / 邮箱验证码）
     *
     * @param command 重置密码命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordCommand command) {
        if (command.getVerifyIdentityType() == null || command.getIdentifier() == null || command.getIdentifier().isBlank()
                || command.getNewPassword() == null || command.getNewPassword().isBlank()) {
            throw new BizException(UserErrorCode.INVALID_PARAM);
        }

        IdentityType verifyIdentityType = IdentityType.of(command.getVerifyIdentityType());
        if (verifyIdentityType != IdentityType.PHONE_SMS && verifyIdentityType != IdentityType.EMAIL_PASSWORD) {
            throw new BizException(UserErrorCode.INVALID_PARAM);
        }

        String normalizedIdentifier = verifyIdentityType == IdentityType.EMAIL_PASSWORD
                ? command.getIdentifier().trim().toLowerCase()
                : command.getIdentifier().trim();

        UserIdentityEntity verifyIdentity = userRepository.findIdentityByTypeAndIdentifier(verifyIdentityType.getCode(), normalizedIdentifier);
        if (verifyIdentity == null) {
            throw new BizException(UserErrorCode.IDENTITY_NOT_FOUND);
        }

        if (command.getEstabId() != null && !userRepository.hasActiveEstabMembership(verifyIdentity.getUserId(), command.getEstabId())) {
            throw new BizException(UserErrorCode.USER_NOT_FOUND);
        }

        UserIdentityEntity passwordIdentity = resolvePasswordIdentity(verifyIdentity.getUserId(), verifyIdentityType);
        if (passwordIdentity == null) {
            throw new BizException(UserErrorCode.PASSWORD_RESET_NOT_SUPPORTED);
        }

        userRepository.updateIdentityCredential(
                passwordIdentity.getId(),
                passwordEncoder.encode(command.getNewPassword()),
                "bcrypt",
                1,
                LocalDateTime.now()
        );
        userRepository.resetLoginFailCount(verifyIdentity.getUserId());
    }

    /**
     * 解析需要更新的密码身份
     *
     * @param userId              用户ID
     * @param verifyIdentityType  本次校验使用的身份类型
     * @return 密码身份
     */
    private UserIdentityEntity resolvePasswordIdentity(Long userId, IdentityType verifyIdentityType) {
        if (userId == null) {
            return null;
        }

        if (verifyIdentityType == IdentityType.EMAIL_PASSWORD) {
            UserIdentityEntity emailIdentity = userRepository.findIdentityByUserIdAndType(userId, IdentityType.EMAIL_PASSWORD.getCode());
            if (emailIdentity != null) {
                return emailIdentity;
            }
        }

        UserIdentityEntity usernameIdentity = userRepository.findIdentityByUserIdAndType(userId, IdentityType.USERNAME_PASSWORD.getCode());
        if (usernameIdentity != null) {
            return usernameIdentity;
        }

        return userRepository.findIdentityByUserIdAndType(userId, IdentityType.EMAIL_PASSWORD.getCode());
    }

    /**
     * 查询用户信息
     *
     * @param command 查询命令
     * @return 用户信息
     */
    public UserInfoDTO queryUserInfo(QueryUserInfoCommand command) {
        if (command.getUserId() == null) {
            throw new BizException(UserErrorCode.INVALID_PARAM);
        }

        UserEntity user = userRepository.findUserById(command.getUserId());
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new BizException(UserErrorCode.USER_NOT_FOUND);
        }

        Long teamId = userRepository.findFirstTeamId(user.getId());
        Long resolvedEstabId = command.getEstabId() != null ? command.getEstabId() : user.getPrimaryEstabId();
        Boolean estabAdmin = userRepository.isEstabAdmin(user.getId(), resolvedEstabId);
        return userDomainAssembler.toUserInfoDto(user, teamId, estabAdmin);
    }

    /**
     * 解析组织ID
     *
     * @param estabId   组织ID
     * @param estabCode 组织编号
     * @return 组织ID
     */
    private Long resolveEstabIdInternal(Long estabId, String estabCode) {
        Long resolved = userRepository.findEstabId(estabId, estabCode);
        boolean hasEstabCondition = estabId != null || (estabCode != null && !estabCode.isBlank());
        if (hasEstabCondition && resolved == null) {
            throw new BizException(UserErrorCode.ESTAB_NOT_FOUND);
        }
        return resolved;
    }

    /**
     * 获取注册类型
     *
     * @param registerType 注册类型
     * @return 注册类型
     */
    private RegisterType requireRegisterType(Integer registerType) {
        RegisterType type = RegisterType.of(registerType);
        if (type == null) {
            throw new BizException(UserErrorCode.REGISTER_TYPE_NOT_SUPPORTED);
        }
        return type;
    }

    /**
     * 映射注册类型到身份类型
     *
     * @param registerType 注册类型
     * @return 身份类型
     */
    private IdentityType mapIdentityType(RegisterType registerType) {
        return switch (registerType) {
            case USERNAME -> IdentityType.USERNAME_PASSWORD;
            case PHONE -> IdentityType.PHONE_SMS;
            case EMAIL -> IdentityType.EMAIL_PASSWORD;
        };
    }

    /**
     * 确保身份不存在
     *
     * @param identityType 身份类型
     * @param identifier   身份标识
     */
    private void ensureIdentityNotExists(Integer identityType, String identifier) {
        long count = userRepository.countIdentity(identityType, identifier);
        if (count > 0) {
            throw new BizException(UserErrorCode.DUPLICATE_IDENTITY);
        }
    }

    /**
     * 构建用户
     *
     * @param registerType 注册类型
     * @param command      注册命令
     * @return 用户
     */
    private UserEntity buildUser(RegisterType registerType, RegisterUserCommand command) {
        UserEntity user = new UserEntity();
        user.setUserCode(generateUserCode());
        user.setUsername(registerType == RegisterType.USERNAME ? command.getIdentifier() : null);
        user.setDisplayName(command.getDisplayName() == null ? command.getIdentifier() : command.getDisplayName());
        user.setNickname(command.getNickname());
        user.setAvatarUrl(command.getAvatarUrl());
        user.setUserType(command.getUserType() != null ? command.getUserType() : UserType.TENANT.getCode());
        user.setStatus(UserStatus.ENABLED.getCode());
        user.setLoginFailCount(0);
        if (registerType == RegisterType.PHONE) {
            user.setPrimaryPhone(command.getIdentifier());
            user.setPhoneVerified(1);
        } else if (registerType == RegisterType.EMAIL) {
            user.setPrimaryEmail(command.getIdentifier());
            user.setEmailVerified(1);
        }
        return user;
    }

    /**
     * 构建身份
     *
     * @param identityType 身份类型
     * @param registerType 注册类型
     * @param command      注册命令
     * @param userId       用户ID
     * @return 身份
     */
    private UserIdentityEntity buildIdentity(Integer identityType, RegisterType registerType, RegisterUserCommand command, Long userId) {
        UserIdentityEntity identity = new UserIdentityEntity();
        identity.setUserId(userId);
        identity.setIdentityType(identityType);
        identity.setIdentifier(command.getIdentifier());
        identity.setIssuer("");
        identity.setStatus(1);
        identity.setIsPrimary(1);
        LocalDateTime now = LocalDateTime.now();
        identity.setVerified(registerType == RegisterType.PHONE || registerType == RegisterType.EMAIL ? 1 : 0);
        identity.setVerifiedAt(registerType == RegisterType.PHONE || registerType == RegisterType.EMAIL ? now : null);
        identity.setBindTime(now);
        if (registerType == RegisterType.USERNAME) {
            if (command.getPassword() == null || command.getPassword().isBlank()) {
                throw new BizException(UserErrorCode.INVALID_PARAM);
            }
            identity.setCredential(passwordEncoder.encode(command.getPassword()));
            identity.setCredentialAlg("bcrypt");
        } else if (registerType == RegisterType.EMAIL && command.getPassword() != null && !command.getPassword().isBlank()) {
            identity.setCredential(passwordEncoder.encode(command.getPassword()));
            identity.setCredentialAlg("bcrypt");
        }
        return identity;
    }

    /**
     * 生成用户编号
     *
     * @return 用户编号
     */
    private String generateUserCode() {
        return "U" + System.currentTimeMillis();
    }

    /**
     * 生成组织编号
     *
     * @return 组织编号
     */
    private String generateEstabCode() {
        return "E" + System.currentTimeMillis();
    }
}
