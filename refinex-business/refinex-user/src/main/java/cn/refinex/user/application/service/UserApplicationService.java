package cn.refinex.user.application.service;

import cn.refinex.api.user.enums.UserStatus;
import cn.refinex.api.user.enums.UserType;
import cn.refinex.api.user.model.dto.*;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.PageResponse;
import cn.refinex.base.utils.PageUtils;
import cn.refinex.file.api.FileService;
import cn.refinex.user.application.assembler.UserDomainAssembler;
import cn.refinex.user.application.command.*;
import cn.refinex.user.application.dto.*;
import cn.refinex.user.domain.error.UserErrorCode;
import cn.refinex.user.domain.model.entity.UserAuthSubject;
import cn.refinex.user.domain.model.entity.UserEntity;
import cn.refinex.user.domain.model.entity.UserEstabEntity;
import cn.refinex.user.domain.model.entity.UserIdentityEntity;
import cn.refinex.user.domain.model.enums.IdentityType;
import cn.refinex.user.domain.model.enums.RegisterType;
import cn.refinex.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static cn.refinex.base.utils.ValueUtils.defaultIfNull;
import static cn.refinex.base.utils.ValueUtils.isBlank;

/**
 * 用户应用服务
 *
 * @author refinex
 */
@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private static final String DEFAULT_ESTAB_NAME = "默认组织";
    private static final String DEFAULT_USER_CODE_PREFIX = "U";
    private static final String DEFAULT_PASSWORD_ENCODER = "bcrypt";
    private static final DateTimeFormatter AVATAR_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final Set<String> ALLOWED_AVATAR_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final long AVATAR_MAX_SIZE_BYTES = 5L * 1024 * 1024;

    private final UserRepository userRepository;
    private final UserDomainAssembler userDomainAssembler;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

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
                DEFAULT_PASSWORD_ENCODER,
                1,
                LocalDateTime.now()
        );
        userRepository.resetLoginFailCount(verifyIdentity.getUserId());
    }

    /**
     * 解析需要更新的密码身份
     *
     * @param userId             用户ID
     * @param verifyIdentityType 本次校验使用的身份类型
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
        if (resolvedEstabId != null && !userRepository.hasActiveEstabMembership(user.getId(), resolvedEstabId)) {
            throw new BizException(UserErrorCode.USER_NOT_FOUND);
        }
        Boolean estabAdmin = userRepository.isEstabAdmin(user.getId(), resolvedEstabId);
        return userDomainAssembler.toUserInfoDto(user, teamId, estabAdmin);
    }

    /**
     * 查询当前用户所属企业列表
     *
     * @param userId         用户ID
     * @param currentEstabId 当前企业ID
     * @return 企业列表
     */
    public List<UserEstabDTO> listUserEstabs(Long userId, Long currentEstabId) {
        if (userId == null) {
            throw new BizException(UserErrorCode.INVALID_PARAM);
        }

        UserEntity user = userRepository.findUserById(userId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new BizException(UserErrorCode.USER_NOT_FOUND);
        }

        Long effectiveCurrentEstabId = currentEstabId != null ? currentEstabId : user.getPrimaryEstabId();
        List<UserEstabEntity> entities = userRepository.listActiveUserEstabs(userId);
        List<UserEstabDTO> result = new ArrayList<>();
        for (UserEstabEntity entity : entities) {
            UserEstabDTO dto = new UserEstabDTO();
            dto.setEstabId(entity.getEstabId());
            dto.setEstabCode(entity.getEstabCode());
            dto.setEstabName(entity.getEstabName());
            dto.setEstabShortName(entity.getEstabShortName());
            dto.setLogoUrl(entity.getLogoUrl());
            dto.setEstabType(entity.getEstabType());
            dto.setAdmin(entity.getIsAdmin() != null && entity.getIsAdmin() == 1);
            dto.setCurrent(effectiveCurrentEstabId != null && effectiveCurrentEstabId.equals(entity.getEstabId()));
            result.add(dto);
        }
        return result;
    }

    /**
     * 查询当前用户账号基础信息
     *
     * @param userId 用户ID
     * @return 账号信息
     */
    public UserAccountDTO queryUserAccountInfo(Long userId) {
        if (userId == null) {
            throw new BizException(UserErrorCode.INVALID_PARAM);
        }

        UserEntity user = userRepository.findUserById(userId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new BizException(UserErrorCode.USER_NOT_FOUND);
        }

        UserIdentityEntity usernamePasswordIdentity = userRepository.findIdentityByUserIdAndType(userId, IdentityType.USERNAME_PASSWORD.getCode());
        UserIdentityEntity emailPasswordIdentity = userRepository.findIdentityByUserIdAndType(userId, IdentityType.EMAIL_PASSWORD.getCode());

        UserAccountDTO dto = new UserAccountDTO();
        dto.setUserId(user.getId());
        dto.setUserCode(user.getUserCode());
        dto.setUsername(user.getUsername());
        dto.setPrimaryPhone(user.getPrimaryPhone());
        dto.setPhoneVerified(user.getPhoneVerified() != null && user.getPhoneVerified() == 1);
        dto.setPrimaryEmail(user.getPrimaryEmail());
        dto.setEmailVerified(user.getEmailVerified() != null && user.getEmailVerified() == 1);
        dto.setStatus(UserStatus.of(user.getStatus()));
        dto.setUserType(UserType.of(user.getUserType()));
        dto.setRegisterTime(user.getGmtCreate());
        dto.setLastLoginTime(user.getLastLoginTime());
        dto.setLastLoginIp(user.getLastLoginIp());
        dto.setUsernamePasswordEnabled(hasCredential(usernamePasswordIdentity));
        dto.setEmailPasswordEnabled(hasCredential(emailPasswordIdentity));
        return dto;
    }

    /**
     * 查询用户管理列表
     *
     * @param query 查询条件
     * @return 用户管理列表
     */
    public PageResponse<UserManageDTO> listManageUsers(UserManageListQuery query) {
        UserManageListQuery safeQuery = query == null ? new UserManageListQuery() : query;
        int currentPage = PageUtils.normalizeCurrentPage(safeQuery.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(
                safeQuery.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE,
                PageUtils.DEFAULT_MAX_PAGE_SIZE
        );

        PageResponse<UserEntity> page = userRepository.listUsersForManage(
                safeQuery.getPrimaryEstabId(),
                safeQuery.getStatus(),
                safeQuery.getUserType(),
                normalizeNullableText(safeQuery.getUserCode()),
                normalizeNullableText(safeQuery.getUsername()),
                normalizeNullableText(safeQuery.getDisplayName()),
                normalizeNullableText(safeQuery.getNickname()),
                normalizeNullableText(safeQuery.getPrimaryPhone()),
                normalizeNullableText(safeQuery.getPrimaryEmail()),
                normalizeNullableText(safeQuery.getKeyword()),
                safeQuery.getUserIds(),
                currentPage,
                pageSize
        );

        List<UserManageDTO> result = new ArrayList<>();
        for (UserEntity user : page.getData()) {
            result.add(toUserManageDto(user));
        }
        enrichPrimaryEstabNames(result);
        return PageResponse.of(result, page.getTotal(), page.getPageSize(), page.getCurrentPage());
    }

    /**
     * 查询用户管理详情
     *
     * @param userId 用户ID
     * @return 用户管理详情
     */
    public UserManageDTO getManageUser(Long userId) {
        UserEntity user = requireUserEntity(userId);
        UserManageDTO dto = toUserManageDto(user);
        enrichPrimaryEstabName(dto);
        return dto;
    }

    /**
     * 创建用户（管理端）
     *
     * @param command 创建命令
     * @return 用户管理详情
     */
    @Transactional(rollbackFor = Exception.class)
    public UserManageDTO createManageUser(UserManageCreateCommand command) {
        if (command == null || isBlank(command.getDisplayName())) {
            throw new BizException(UserErrorCode.INVALID_PARAM);
        }

        String userCode = normalizeNullableText(command.getUserCode());
        if (isBlank(userCode)) {
            userCode = generateUserCode();
        }
        if (userRepository.countUserCode(userCode, null) > 0) {
            throw new BizException(UserErrorCode.USER_CODE_DUPLICATED);
        }

        String username = normalizeNullableText(command.getUsername());
        if (!isBlank(username) && userRepository.countUsername(username, null) > 0) {
            throw new BizException(UserErrorCode.USERNAME_DUPLICATED);
        }

        Long primaryEstabId = command.getPrimaryEstabId();
        if (primaryEstabId != null && userRepository.findEstabId(primaryEstabId, null) == null) {
            throw new BizException(UserErrorCode.ESTAB_NOT_FOUND);
        }

        UserEntity user = new UserEntity();
        user.setUserCode(userCode);
        user.setUsername(username);
        user.setDisplayName(command.getDisplayName().trim());
        user.setNickname(normalizeNullableText(command.getNickname()));
        user.setAvatarUrl(normalizeNullableText(command.getAvatarUrl()));
        user.setGender(defaultIfNull(command.getGender(), 0));
        user.setBirthday(command.getBirthday());
        user.setUserType(defaultIfNull(command.getUserType(), UserType.TENANT.getCode()));
        user.setStatus(defaultIfNull(command.getStatus(), UserStatus.ENABLED.getCode()));
        user.setPrimaryEstabId(primaryEstabId);
        user.setPrimaryPhone(normalizeNullableText(command.getPrimaryPhone()));
        user.setPhoneVerified(defaultIfNull(command.getPhoneVerified(), 0));
        user.setPrimaryEmail(normalizeNullableText(command.getPrimaryEmail()));
        user.setEmailVerified(defaultIfNull(command.getEmailVerified(), 0));
        user.setLoginFailCount(0);
        user.setRemark(normalizeNullableText(command.getRemark()));

        UserEntity created = userRepository.insertUser(user);
        UserManageDTO dto = toUserManageDto(requireUserEntity(created.getId()));
        enrichPrimaryEstabName(dto);
        return dto;
    }

    /**
     * 更新用户（管理端）
     *
     * @param userId  用户ID
     * @param command 更新命令
     * @return 用户管理详情
     */
    @Transactional(rollbackFor = Exception.class)
    public UserManageDTO updateManageUser(Long userId, UserManageUpdateCommand command) {
        if (command == null || userId == null || isBlank(command.getDisplayName())) {
            throw new BizException(UserErrorCode.INVALID_PARAM);
        }

        UserEntity existing = requireUserEntity(userId);
        Long primaryEstabId = command.getPrimaryEstabId();
        if (primaryEstabId != null && userRepository.findEstabId(primaryEstabId, null) == null) {
            throw new BizException(UserErrorCode.ESTAB_NOT_FOUND);
        }

        existing.setDisplayName(command.getDisplayName().trim());
        existing.setNickname(normalizeNullableText(command.getNickname()));
        existing.setAvatarUrl(normalizeNullableText(command.getAvatarUrl()));
        existing.setGender(defaultIfNull(command.getGender(), existing.getGender()));
        existing.setBirthday(command.getBirthday());
        existing.setUserType(defaultIfNull(command.getUserType(), existing.getUserType()));
        existing.setStatus(defaultIfNull(command.getStatus(), existing.getStatus()));
        existing.setPrimaryEstabId(primaryEstabId);
        existing.setPrimaryPhone(normalizeNullableText(command.getPrimaryPhone()));
        existing.setPhoneVerified(defaultIfNull(command.getPhoneVerified(), existing.getPhoneVerified()));
        existing.setPrimaryEmail(normalizeNullableText(command.getPrimaryEmail()));
        existing.setEmailVerified(defaultIfNull(command.getEmailVerified(), existing.getEmailVerified()));
        existing.setRemark(normalizeNullableText(command.getRemark()));

        userRepository.updateUser(existing);
        UserManageDTO dto = toUserManageDto(requireUserEntity(existing.getId()));
        enrichPrimaryEstabName(dto);
        return dto;
    }

    /**
     * 查询管理端用户所属企业列表
     *
     * @param userId 用户ID
     * @return 用户所属企业列表
     */
    public List<UserManageEstabDTO> listManageUserEstabs(Long userId) {
        List<UserEstabDTO> estabs = listUserEstabs(userId, null);
        List<UserManageEstabDTO> result = new ArrayList<>();
        for (UserEstabDTO estab : estabs) {
            if (estab == null) {
                continue;
            }

            UserManageEstabDTO dto = new UserManageEstabDTO();
            dto.setEstabId(estab.getEstabId());
            dto.setEstabCode(estab.getEstabCode());
            dto.setEstabName(estab.getEstabName());
            dto.setEstabShortName(estab.getEstabShortName());
            dto.setLogoUrl(estab.getLogoUrl());
            dto.setEstabType(estab.getEstabType());
            dto.setAdmin(estab.getAdmin());
            dto.setCurrent(estab.getCurrent());
            result.add(dto);
        }
        return result;
    }

    /**
     * 查询用户身份列表（管理端）
     *
     * @param userId 用户ID
     * @return 身份列表
     */
    public List<UserIdentityManageDTO> listManageIdentities(Long userId) {
        requireUserEntity(userId);
        List<UserIdentityEntity> identities = userRepository.listIdentitiesByUserId(userId);
        List<UserIdentityManageDTO> result = new ArrayList<>();
        for (UserIdentityEntity identity : identities) {
            result.add(toIdentityManageDto(identity));
        }
        return result;
    }

    /**
     * 创建用户身份（管理端）
     *
     * @param userId  用户ID
     * @param command 创建命令
     * @return 用户身份
     */
    @Transactional(rollbackFor = Exception.class)
    public UserIdentityManageDTO createManageIdentity(Long userId, UserIdentityManageCreateCommand command) {
        if (command == null || userId == null || command.getIdentityType() == null || isBlank(command.getIdentifier())) {
            throw new BizException(UserErrorCode.INVALID_PARAM);
        }
        requireUserEntity(userId);

        String identifier = normalizeIdentifier(command.getIdentityType(), command.getIdentifier());
        String issuer = normalizeNullableText(command.getIssuer());
        if (issuer == null) {
            issuer = "";
        }
        if (userRepository.countIdentityByUnique(command.getIdentityType(), identifier, issuer, null) > 0) {
            throw new BizException(UserErrorCode.DUPLICATE_IDENTITY);
        }

        UserIdentityEntity identity = new UserIdentityEntity();
        identity.setUserId(userId);
        identity.setIdentityType(command.getIdentityType());
        identity.setIdentifier(identifier);
        identity.setIssuer(issuer);
        identity.setCredential(normalizeNullableText(command.getCredential()));
        identity.setCredentialAlg(normalizeNullableText(command.getCredentialAlg()));
        identity.setIsPrimary(defaultIfNull(command.getIsPrimary(), 0));
        identity.setVerified(defaultIfNull(command.getVerified(), 0));
        identity.setVerifiedAt(identity.getVerified() == 1 ? LocalDateTime.now() : null);
        identity.setBindTime(LocalDateTime.now());
        identity.setStatus(defaultIfNull(command.getStatus(), 1));

        UserIdentityEntity created = userRepository.insertIdentity(identity);
        if (created.getIsPrimary() != null && created.getIsPrimary() == 1) {
            userRepository.clearPrimaryIdentity(userId, created.getId());
        }
        return toIdentityManageDto(requireIdentityEntity(created.getId()));
    }

    /**
     * 更新用户身份（管理端）
     *
     * @param identityId 身份ID
     * @param command    更新命令
     * @return 用户身份
     */
    @Transactional(rollbackFor = Exception.class)
    public UserIdentityManageDTO updateManageIdentity(Long identityId, UserIdentityManageUpdateCommand command) {
        if (command == null || identityId == null) {
            throw new BizException(UserErrorCode.INVALID_PARAM);
        }

        UserIdentityEntity existing = requireIdentityEntity(identityId);
        String issuer = command.getIssuer() == null ? existing.getIssuer() : normalizeNullableText(command.getIssuer());
        if (issuer == null) {
            issuer = "";
        }
        String identifier = command.getIdentifier() == null
                ? existing.getIdentifier()
                : normalizeIdentifier(existing.getIdentityType(), command.getIdentifier());
        if (userRepository.countIdentityByUnique(existing.getIdentityType(), identifier, issuer, existing.getId()) > 0) {
            throw new BizException(UserErrorCode.DUPLICATE_IDENTITY);
        }

        existing.setIdentifier(identifier);
        existing.setIssuer(issuer);
        if (command.getCredential() != null) {
            existing.setCredential(normalizeNullableText(command.getCredential()));
        }
        if (command.getCredentialAlg() != null) {
            existing.setCredentialAlg(normalizeNullableText(command.getCredentialAlg()));
        }
        if (command.getIsPrimary() != null) {
            existing.setIsPrimary(command.getIsPrimary());
        }
        if (command.getVerified() != null) {
            existing.setVerified(command.getVerified());
            existing.setVerifiedAt(command.getVerified() == 1 ? LocalDateTime.now() : null);
        }
        if (command.getStatus() != null) {
            existing.setStatus(command.getStatus());
        }

        userRepository.updateIdentity(existing);
        if (existing.getIsPrimary() != null && existing.getIsPrimary() == 1) {
            userRepository.clearPrimaryIdentity(existing.getUserId(), existing.getId());
        }
        return toIdentityManageDto(requireIdentityEntity(existing.getId()));
    }

    /**
     * 删除用户身份（管理端）
     *
     * @param identityId 身份ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteManageIdentity(Long identityId) {
        UserIdentityEntity existing = requireIdentityEntity(identityId);
        long identityCount = userRepository.countIdentityByUserId(existing.getUserId());
        if (identityCount <= 1) {
            throw new BizException(UserErrorCode.LAST_IDENTITY_NOT_ALLOW_DELETE);
        }
        userRepository.deleteIdentityById(identityId);
    }

    /**
     * 当前用户修改密码
     *
     * @param command 修改密码命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordCommand command) {
        if (command == null || command.getUserId() == null
                || command.getOldPassword() == null || command.getOldPassword().isBlank()
                || command.getNewPassword() == null || command.getNewPassword().isBlank()) {
            throw new BizException(UserErrorCode.INVALID_PARAM);
        }

        UserEntity user = userRepository.findUserById(command.getUserId());
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new BizException(UserErrorCode.USER_NOT_FOUND);
        }

        UserIdentityEntity usernamePasswordIdentity = userRepository.findIdentityByUserIdAndType(command.getUserId(), IdentityType.USERNAME_PASSWORD.getCode());
        UserIdentityEntity emailPasswordIdentity = userRepository.findIdentityByUserIdAndType(command.getUserId(), IdentityType.EMAIL_PASSWORD.getCode());

        List<UserIdentityEntity> passwordIdentities = new ArrayList<>();
        if (hasCredential(usernamePasswordIdentity)) {
            passwordIdentities.add(usernamePasswordIdentity);
        }
        if (hasCredential(emailPasswordIdentity)) {
            passwordIdentities.add(emailPasswordIdentity);
        }
        if (passwordIdentities.isEmpty()) {
            throw new BizException(UserErrorCode.PASSWORD_RESET_NOT_SUPPORTED);
        }

        boolean oldPasswordMatched = false;
        for (UserIdentityEntity identity : passwordIdentities) {
            if (passwordEncoder.matches(command.getOldPassword(), identity.getCredential())) {
                oldPasswordMatched = true;
                break;
            }
        }
        if (!oldPasswordMatched) {
            throw new BizException(UserErrorCode.OLD_PASSWORD_INCORRECT);
        }

        LocalDateTime now = LocalDateTime.now();
        String encodedPassword = passwordEncoder.encode(command.getNewPassword());
        for (UserIdentityEntity identity : passwordIdentities) {
            userRepository.updateIdentityCredential(
                    identity.getId(),
                    encodedPassword,
                    DEFAULT_PASSWORD_ENCODER,
                    identity.getVerified(),
                    identity.getVerifiedAt() == null ? now : identity.getVerifiedAt()
            );
        }
        userRepository.resetLoginFailCount(command.getUserId());
    }

    /**
     * 上传用户头像
     *
     * @param command     上传头像命令
     * @param inputStream 文件流
     * @return 用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserInfoDTO uploadUserAvatar(UploadUserAvatarCommand command, InputStream inputStream) {
        if (command == null || command.getUserId() == null || inputStream == null) {
            throw new BizException(UserErrorCode.INVALID_PARAM);
        }

        if (command.getFileSize() == null || command.getFileSize() <= 0) {
            throw new BizException("头像文件不能为空", UserErrorCode.INVALID_PARAM);
        }
        if (command.getFileSize() > AVATAR_MAX_SIZE_BYTES) {
            throw new BizException("头像文件大小不能超过 5MB", UserErrorCode.INVALID_PARAM);
        }

        UserEntity user = userRepository.findUserById(command.getUserId());
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new BizException(UserErrorCode.USER_NOT_FOUND);
        }

        String extension = resolveAvatarExtension(command.getOriginalFilename(), command.getContentType());
        String path = buildAvatarPath(command.getUserId(), extension);
        String avatarUrl = fileService.upload(path, inputStream);
        userRepository.updateUserAvatar(command.getUserId(), avatarUrl);

        QueryUserInfoCommand query = new QueryUserInfoCommand();
        query.setUserId(command.getUserId());
        query.setEstabId(command.getEstabId());
        return queryUserInfo(query);
    }

    /**
     * 更新用户资料
     *
     * @param command 更新命令
     * @return 用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserInfoDTO updateUserProfile(UpdateUserProfileCommand command) {
        if (command.getUserId() == null || command.getDisplayName() == null || command.getDisplayName().isBlank()) {
            throw new BizException(UserErrorCode.INVALID_PARAM);
        }

        UserEntity user = userRepository.findUserById(command.getUserId());
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new BizException(UserErrorCode.USER_NOT_FOUND);
        }

        userRepository.updateUserProfile(
                command.getUserId(),
                command.getDisplayName().trim(),
                normalizeNullableText(command.getNickname()),
                normalizeNullableText(command.getAvatarUrl()),
                command.getGender(),
                command.getBirthday()
        );

        QueryUserInfoCommand query = new QueryUserInfoCommand();
        query.setUserId(command.getUserId());
        query.setEstabId(command.getEstabId());
        return queryUserInfo(query);
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
            identity.setCredentialAlg(DEFAULT_PASSWORD_ENCODER);
        } else if (registerType == RegisterType.EMAIL && command.getPassword() != null && !command.getPassword().isBlank()) {
            identity.setCredential(passwordEncoder.encode(command.getPassword()));
            identity.setCredentialAlg(DEFAULT_PASSWORD_ENCODER);
        }
        return identity;
    }

    /**
     * 校验并返回用户实体
     *
     * @param userId 用户ID
     * @return 用户实体
     */
    private UserEntity requireUserEntity(Long userId) {
        if (userId == null) {
            throw new BizException(UserErrorCode.INVALID_PARAM);
        }
        UserEntity user = userRepository.findUserById(userId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new BizException(UserErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    /**
     * 校验并返回身份实体
     *
     * @param identityId 身份ID
     * @return 身份实体
     */
    private UserIdentityEntity requireIdentityEntity(Long identityId) {
        if (identityId == null) {
            throw new BizException(UserErrorCode.INVALID_PARAM);
        }
        UserIdentityEntity identity = userRepository.findIdentityById(identityId);
        if (identity == null || (identity.getDeleted() != null && identity.getDeleted() == 1)) {
            throw new BizException(UserErrorCode.IDENTITY_NOT_FOUND);
        }
        return identity;
    }

    /**
     * 将用户实体转换为管理端 DTO
     *
     * @param user 用户实体
     * @return 用户管理 DTO
     */
    private UserManageDTO toUserManageDto(UserEntity user) {
        UserManageDTO dto = new UserManageDTO();
        dto.setUserId(user.getId());
        dto.setUserCode(user.getUserCode());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName());
        dto.setNickname(user.getNickname());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setGender(user.getGender());
        dto.setBirthday(user.getBirthday());
        dto.setUserType(user.getUserType());
        dto.setStatus(user.getStatus());
        dto.setPrimaryEstabId(user.getPrimaryEstabId());
        dto.setPrimaryPhone(user.getPrimaryPhone());
        dto.setPhoneVerified(user.getPhoneVerified());
        dto.setPrimaryEmail(user.getPrimaryEmail());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setLastLoginTime(user.getLastLoginTime());
        dto.setLastLoginIp(user.getLastLoginIp());
        dto.setLoginFailCount(user.getLoginFailCount());
        dto.setLockUntil(user.getLockUntil());
        dto.setRemark(user.getRemark());
        return dto;
    }

    /**
     * 批量补充主企业名称
     *
     * @param users 用户列表
     */
    private void enrichPrimaryEstabNames(List<UserManageDTO> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        List<Long> estabIds = users.stream()
                .map(UserManageDTO::getPrimaryEstabId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> estabNameMap = userRepository.listEstabNameMapByIds(estabIds);
        for (UserManageDTO user : users) {
            if (user == null) {
                continue;
            }
            user.setPrimaryEstabName(estabNameMap.get(user.getPrimaryEstabId()));
        }
    }

    /**
     * 补充单个用户主企业名称
     *
     * @param user 用户
     */
    private void enrichPrimaryEstabName(UserManageDTO user) {
        if (user == null || user.getPrimaryEstabId() == null) {
            return;
        }
        Map<Long, String> estabNameMap = userRepository.listEstabNameMapByIds(List.of(user.getPrimaryEstabId()));
        user.setPrimaryEstabName(estabNameMap.get(user.getPrimaryEstabId()));
    }

    /**
     * 将身份实体转换为管理端 DTO
     *
     * @param identity 身份实体
     * @return 身份管理 DTO
     */
    private UserIdentityManageDTO toIdentityManageDto(UserIdentityEntity identity) {
        UserIdentityManageDTO dto = new UserIdentityManageDTO();
        dto.setIdentityId(identity.getId());
        dto.setUserId(identity.getUserId());
        dto.setIdentityType(identity.getIdentityType());
        dto.setIdentifier(identity.getIdentifier());
        dto.setIssuer(identity.getIssuer());
        dto.setCredentialAlg(identity.getCredentialAlg());
        dto.setIsPrimary(identity.getIsPrimary());
        dto.setVerified(identity.getVerified());
        dto.setVerifiedAt(identity.getVerifiedAt());
        dto.setBindTime(identity.getBindTime());
        dto.setLastLoginTime(identity.getLastLoginTime());
        dto.setLastLoginIp(identity.getLastLoginIp());
        dto.setStatus(identity.getStatus());
        return dto;
    }

    /**
     * 规范化身份标识
     *
     * @param identityType 身份类型
     * @param identifier   身份标识
     * @return 规范化后的身份标识
     */
    private String normalizeIdentifier(Integer identityType, String identifier) {
        String normalized = normalizeNullableText(identifier);
        if (normalized == null) {
            throw new BizException(UserErrorCode.INVALID_PARAM);
        }
        if (IdentityType.EMAIL_PASSWORD.getCode() == identityType
                || IdentityType.EMAIL_CODE.getCode() == identityType) {
            return normalized.toLowerCase(Locale.ROOT);
        }
        return normalized;
    }

    /**
     * 生成用户编号
     *
     * @return 用户编号
     */
    private String generateUserCode() {
        return DEFAULT_USER_CODE_PREFIX + System.currentTimeMillis();
    }

    /**
     * 生成组织编号
     *
     * @return 组织编号
     */
    private String generateEstabCode() {
        return "E" + System.currentTimeMillis();
    }

    /**
     * 解析头像扩展名
     *
     * @param originalFilename 原始文件名
     * @param contentType      内容类型
     * @return 扩展名
     */
    private String resolveAvatarExtension(String originalFilename, String contentType) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (StringUtils.hasText(extension)) {
            String normalized = extension.toLowerCase(Locale.ROOT);
            if (ALLOWED_AVATAR_EXTENSIONS.contains(normalized)) {
                return normalized;
            }
        }

        if (StringUtils.hasText(contentType)) {
            return switch (contentType.toLowerCase(Locale.ROOT)) {
                case "image/jpeg", "image/jpg" -> "jpg";
                case "image/png" -> "png";
                case "image/webp" -> "webp";
                case "image/gif" -> "gif";
                default -> throw new BizException("头像仅支持 JPG/PNG/WEBP/GIF 格式", UserErrorCode.INVALID_PARAM);
            };
        }

        throw new BizException("头像仅支持 JPG/PNG/WEBP/GIF 格式", UserErrorCode.INVALID_PARAM);
    }

    /**
     * 构建头像路径
     *
     * @param userId    用户ID
     * @param extension 扩展名
     * @return 路径
     */
    private String buildAvatarPath(Long userId, String extension) {
        String folder = LocalDateTime.now().format(AVATAR_DATE_FORMATTER);
        String fileName = "u_" + userId + "_" + UUID.randomUUID().toString().replace("-", "");
        return "avatar/user/" + folder + "/" + fileName + "." + extension;
    }

    /**
     * 是否有凭证
     *
     * @param identity 身份
     * @return 是否有凭证
     */
    private boolean hasCredential(UserIdentityEntity identity) {
        return identity != null
                && identity.getStatus() != null
                && identity.getStatus() == 1
                && identity.getCredential() != null
                && !identity.getCredential().isBlank();
    }

    /**
     * 规范化可空文本
     *
     * @param value 值
     * @return 规范化后的文本
     */
    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
