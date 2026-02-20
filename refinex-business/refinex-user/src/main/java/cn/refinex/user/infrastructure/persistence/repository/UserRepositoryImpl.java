package cn.refinex.user.infrastructure.persistence.repository;

import cn.refinex.base.response.PageResponse;
import cn.refinex.user.domain.model.entity.UserEntity;
import cn.refinex.user.domain.model.entity.UserEstabEntity;
import cn.refinex.user.domain.model.entity.UserIdentityEntity;
import cn.refinex.user.domain.repository.UserRepository;
import cn.refinex.user.infrastructure.converter.UserDoConverter;
import cn.refinex.user.infrastructure.converter.UserIdentityDoConverter;
import cn.refinex.user.infrastructure.persistence.dataobject.DefEstabDo;
import cn.refinex.user.infrastructure.persistence.dataobject.DefEstabUserDo;
import cn.refinex.user.infrastructure.persistence.dataobject.DefUserDo;
import cn.refinex.user.infrastructure.persistence.dataobject.DefUserIdentityDo;
import cn.refinex.user.infrastructure.persistence.dataobject.UserEstabJoinDo;
import cn.refinex.user.infrastructure.persistence.mapper.DefEstabMapper;
import cn.refinex.user.infrastructure.persistence.mapper.DefEstabUserMapper;
import cn.refinex.user.infrastructure.persistence.mapper.DefTeamUserMapper;
import cn.refinex.user.infrastructure.persistence.mapper.DefUserIdentityMapper;
import cn.refinex.user.infrastructure.persistence.mapper.DefUserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户仓储实现
 *
 * @author refinex
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private static final String LIMIT_ONE = "LIMIT 1";

    private final DefUserMapper defUserMapper;
    private final DefUserIdentityMapper defUserIdentityMapper;
    private final DefEstabMapper defEstabMapper;
    private final DefEstabUserMapper defEstabUserMapper;
    private final DefTeamUserMapper defTeamUserMapper;
    private final UserDoConverter userDoConverter;
    private final UserIdentityDoConverter userIdentityDoConverter;

    /**
     * 统计身份
     *
     * @param identityType 身份类型
     * @param identifier   标识符
     * @return 身份数量
     */
    @Override
    public long countIdentity(Integer identityType, String identifier) {
        Long count = defUserIdentityMapper.selectCount(
                Wrappers.lambdaQuery(DefUserIdentityDo.class)
                        .eq(DefUserIdentityDo::getIdentityType, identityType)
                        .eq(DefUserIdentityDo::getIdentifier, identifier)
                        .eq(DefUserIdentityDo::getDeleted, 0)
        );

        return count == null ? 0L : count;
    }

    /**
     * 插入用户
     *
     * @param user 用户
     * @return 插入后的用户
     */
    @Override
    public UserEntity insertUser(UserEntity user) {
        DefUserDo userDo = userDoConverter.toDo(user);
        defUserMapper.insert(userDo);
        return userDoConverter.toEntity(userDo);
    }

    /**
     * 更新用户
     *
     * @param user 用户
     */
    @Override
    public void updateUser(UserEntity user) {
        DefUserDo userDo = userDoConverter.toDo(user);
        defUserMapper.updateById(userDo);
    }

    /**
     * 根据用户ID查找用户
     *
     * @param userId 用户ID
     * @return 用户
     */
    @Override
    public UserEntity findUserById(Long userId) {
        DefUserDo userDo = defUserMapper.selectById(userId);
        if (userDo == null) {
            return null;
        }

        return userDoConverter.toEntity(userDo);
    }

    /**
     * 查询用户管理列表
     *
     * @param primaryEstabId 主组织ID
     * @param status         用户状态
     * @param userType       用户类型
     * @param keyword        关键字
     * @param currentPage    当前页
     * @param pageSize       每页条数
     * @return 用户分页列表
     */
    @Override
    public PageResponse<UserEntity> listUsersForManage(Long primaryEstabId, Integer status, Integer userType,
                                                       String userCode, String username, String displayName,
                                                       String nickname, String primaryPhone, String primaryEmail,
                                                       String keyword, List<Long> userIds, int currentPage,
                                                       int pageSize) {
        var query = Wrappers.lambdaQuery(DefUserDo.class)
                .eq(DefUserDo::getDeleted, 0)
                .orderByDesc(DefUserDo::getId);
        if (primaryEstabId != null) {
            query.eq(DefUserDo::getPrimaryEstabId, primaryEstabId);
        }
        if (status != null) {
            query.eq(DefUserDo::getStatus, status);
        }
        if (userType != null) {
            query.eq(DefUserDo::getUserType, userType);
        }
        if (userCode != null && !userCode.isBlank()) {
            query.like(DefUserDo::getUserCode, userCode.trim());
        }
        if (username != null && !username.isBlank()) {
            query.like(DefUserDo::getUsername, username.trim());
        }
        if (displayName != null && !displayName.isBlank()) {
            query.like(DefUserDo::getDisplayName, displayName.trim());
        }
        if (nickname != null && !nickname.isBlank()) {
            query.like(DefUserDo::getNickname, nickname.trim());
        }
        if (primaryPhone != null && !primaryPhone.isBlank()) {
            query.like(DefUserDo::getPrimaryPhone, primaryPhone.trim());
        }
        if (primaryEmail != null && !primaryEmail.isBlank()) {
            query.like(DefUserDo::getPrimaryEmail, primaryEmail.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(wrapper -> wrapper
                    .like(DefUserDo::getUsername, trimmed)
                    .or()
                    .like(DefUserDo::getDisplayName, trimmed)
                    .or()
                    .like(DefUserDo::getNickname, trimmed)
                    .or()
                    .like(DefUserDo::getPrimaryPhone, trimmed)
                    .or()
                    .like(DefUserDo::getPrimaryEmail, trimmed)
                    .or()
                    .like(DefUserDo::getUserCode, trimmed));
        }
        if (userIds != null && !userIds.isEmpty()) {
            query.in(DefUserDo::getId, userIds);
        }

        Page<DefUserDo> page = new Page<>(currentPage, pageSize);
        Page<DefUserDo> rows = defUserMapper.selectPage(page, query);
        List<UserEntity> data = rows.getRecords().stream().map(userDoConverter::toEntity).collect(Collectors.toList());
        return PageResponse.of(data, rows.getTotal(), pageSize, currentPage);
    }

    /**
     * 批量查询企业名称映射
     *
     * @param estabIds 企业ID列表
     * @return 企业名称映射
     */
    @Override
    public Map<Long, String> listEstabNameMapByIds(List<Long> estabIds) {
        if (estabIds == null || estabIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<DefEstabDo> rows = defEstabMapper.selectBatchIds(estabIds);
        Map<Long, String> result = new HashMap<>();
        if (rows == null || rows.isEmpty()) {
            return result;
        }
        for (DefEstabDo row : rows) {
            if (row == null || row.getId() == null) {
                continue;
            }
            if (row.getDeleted() != null && row.getDeleted() == 1) {
                continue;
            }
            result.put(row.getId(), row.getEstabName());
        }
        return result;
    }

    /**
     * 根据用户编码统计用户数量
     *
     * @param userCode      用户编码
     * @param excludeUserId 排除用户ID
     * @return 用户数量
     */
    @Override
    public long countUserCode(String userCode, Long excludeUserId) {
        var query = Wrappers.lambdaQuery(DefUserDo.class)
                .eq(DefUserDo::getUserCode, userCode)
                .eq(DefUserDo::getDeleted, 0);
        if (excludeUserId != null) {
            query.ne(DefUserDo::getId, excludeUserId);
        }
        Long count = defUserMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 根据用户名统计用户数量
     *
     * @param username      用户名
     * @param excludeUserId 排除用户ID
     * @return 用户数量
     */
    @Override
    public long countUsername(String username, Long excludeUserId) {
        var query = Wrappers.lambdaQuery(DefUserDo.class)
                .eq(DefUserDo::getUsername, username)
                .eq(DefUserDo::getDeleted, 0);
        if (excludeUserId != null) {
            query.ne(DefUserDo::getId, excludeUserId);
        }
        Long count = defUserMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 更新用户主企业
     *
     * @param userId   用户ID
     * @param estabId  企业ID
     */
    @Override
    public void updateUserPrimaryEstab(Long userId, Long estabId) {
        DefUserDo userDo = new DefUserDo();
        userDo.setId(userId);
        userDo.setPrimaryEstabId(estabId);
        defUserMapper.updateById(userDo);
    }

    /**
     * 插入企业
     *
     * @param estabCode    企业编号
     * @param estabName    企业名称
     * @param ownerUserId  企业所有者用户ID
     * @return 插入后的企业ID
     */
    @Override
    public Long insertEstab(String estabCode, String estabName, Long ownerUserId) {
        DefEstabDo estabDo = new DefEstabDo();
        estabDo.setEstabCode(estabCode);
        estabDo.setEstabName(estabName);
        estabDo.setEstabType(1);
        estabDo.setStatus(1);
        estabDo.setOwnerUserId(ownerUserId);
        defEstabMapper.insert(estabDo);
        return estabDo.getId();
    }

    /**
     * 根据企业ID或企业编号查找企业ID
     *
     * @param estabId  企业ID
     * @param estabCode 企业编号
     * @return 企业ID
     */
    @Override
    public Long findEstabId(Long estabId, String estabCode) {
        if (estabId != null) {
            DefEstabDo estabDo = defEstabMapper.selectOne(
                    Wrappers.lambdaQuery(DefEstabDo.class)
                            .eq(DefEstabDo::getId, estabId)
                            .eq(DefEstabDo::getDeleted, 0)
                            .last(LIMIT_ONE)
            );
            return estabDo == null ? null : estabDo.getId();
        }

        if (estabCode == null || estabCode.isBlank()) {
            return null;
        }

        DefEstabDo estabDo = defEstabMapper.selectOne(
                Wrappers.lambdaQuery(DefEstabDo.class)
                        .eq(DefEstabDo::getEstabCode, estabCode)
                        .eq(DefEstabDo::getDeleted, 0)
                        .last(LIMIT_ONE)
        );
        return estabDo == null ? null : estabDo.getId();
    }

    /**
     * 插入企业用户关系
     *
     * @param estabId   企业ID
     * @param userId    用户ID
     * @param isAdmin   是否管理员
     * @param joinTime  加入时间
     */
    @Override
    public void insertEstabUserRelation(Long estabId, Long userId, Integer isAdmin, LocalDateTime joinTime) {
        DefEstabUserDo estabUserDo = new DefEstabUserDo();
        estabUserDo.setEstabId(estabId);
        estabUserDo.setUserId(userId);
        estabUserDo.setIsAdmin(isAdmin);
        estabUserDo.setStatus(1);
        estabUserDo.setJoinTime(joinTime);
        defEstabUserMapper.insert(estabUserDo);
    }

    /**
     * 插入身份
     *
     * @param identity 身份
     * @return 插入后的身份
     */
    @Override
    public UserIdentityEntity insertIdentity(UserIdentityEntity identity) {
        DefUserIdentityDo identityDo = userIdentityDoConverter.toDo(identity);
        defUserIdentityMapper.insert(identityDo);
        return userIdentityDoConverter.toEntity(identityDo);
    }

    /**
     * 根据身份类型和标识符查找身份
     *
     * @param identityType 身份类型
     * @param identifier   标识符
     * @return 身份
     */
    @Override
    public UserIdentityEntity findIdentityByTypeAndIdentifier(Integer identityType, String identifier) {
        DefUserIdentityDo identityDo = defUserIdentityMapper.selectOne(
                Wrappers.lambdaQuery(DefUserIdentityDo.class)
                        .eq(DefUserIdentityDo::getIdentityType, identityType)
                        .eq(DefUserIdentityDo::getIdentifier, identifier)
                        .eq(DefUserIdentityDo::getDeleted, 0)
                        .last(LIMIT_ONE)
        );

        if (identityDo == null) {
            return null;
        }

        return userIdentityDoConverter.toEntity(identityDo);
    }

    /**
     * 根据用户ID和身份类型查找身份
     *
     * @param userId       用户ID
     * @param identityType 身份类型
     * @return 身份
     */
    @Override
    public UserIdentityEntity findIdentityByUserIdAndType(Long userId, Integer identityType) {
        DefUserIdentityDo identityDo = defUserIdentityMapper.selectOne(
                Wrappers.lambdaQuery(DefUserIdentityDo.class)
                        .eq(DefUserIdentityDo::getUserId, userId)
                        .eq(DefUserIdentityDo::getIdentityType, identityType)
                        .eq(DefUserIdentityDo::getDeleted, 0)
                        .last(LIMIT_ONE)
        );

        if (identityDo == null) {
            return null;
        }

        return userIdentityDoConverter.toEntity(identityDo);
    }

    /**
     * 根据身份ID查询身份
     *
     * @param identityId 身份ID
     * @return 身份
     */
    @Override
    public UserIdentityEntity findIdentityById(Long identityId) {
        DefUserIdentityDo identityDo = defUserIdentityMapper.selectById(identityId);
        return identityDo == null ? null : userIdentityDoConverter.toEntity(identityDo);
    }

    /**
     * 查询用户身份列表
     *
     * @param userId 用户ID
     * @return 身份列表
     */
    @Override
    public List<UserIdentityEntity> listIdentitiesByUserId(Long userId) {
        List<DefUserIdentityDo> rows = defUserIdentityMapper.selectList(
                Wrappers.lambdaQuery(DefUserIdentityDo.class)
                        .eq(DefUserIdentityDo::getUserId, userId)
                        .eq(DefUserIdentityDo::getDeleted, 0)
                        .orderByAsc(DefUserIdentityDo::getIdentityType, DefUserIdentityDo::getId)
        );
        return rows.stream().map(userIdentityDoConverter::toEntity).collect(Collectors.toList());
    }

    /**
     * 根据唯一键统计身份数量
     *
     * @param identityType      身份类型
     * @param identifier        标识符
     * @param issuer            发行方
     * @param excludeIdentityId 排除身份ID
     * @return 身份数量
     */
    @Override
    public long countIdentityByUnique(Integer identityType, String identifier, String issuer, Long excludeIdentityId) {
        var query = Wrappers.lambdaQuery(DefUserIdentityDo.class)
                .eq(DefUserIdentityDo::getIdentityType, identityType)
                .eq(DefUserIdentityDo::getIdentifier, identifier)
                .eq(DefUserIdentityDo::getIssuer, issuer)
                .eq(DefUserIdentityDo::getDeleted, 0);
        if (excludeIdentityId != null) {
            query.ne(DefUserIdentityDo::getId, excludeIdentityId);
        }
        Long count = defUserIdentityMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 统计用户身份数量
     *
     * @param userId 用户ID
     * @return 身份数量
     */
    @Override
    public long countIdentityByUserId(Long userId) {
        Long count = defUserIdentityMapper.selectCount(
                Wrappers.lambdaQuery(DefUserIdentityDo.class)
                        .eq(DefUserIdentityDo::getUserId, userId)
                        .eq(DefUserIdentityDo::getDeleted, 0)
        );
        return count == null ? 0L : count;
    }

    /**
     * 更新身份
     *
     * @param identity 身份
     */
    @Override
    public void updateIdentity(UserIdentityEntity identity) {
        DefUserIdentityDo row = userIdentityDoConverter.toDo(identity);
        defUserIdentityMapper.updateById(row);
    }

    /**
     * 清理用户其他主身份标记
     *
     * @param userId            用户ID
     * @param excludeIdentityId 排除身份ID
     */
    @Override
    public void clearPrimaryIdentity(Long userId, Long excludeIdentityId) {
        var update = Wrappers.lambdaUpdate(DefUserIdentityDo.class)
                .eq(DefUserIdentityDo::getUserId, userId)
                .eq(DefUserIdentityDo::getDeleted, 0)
                .set(DefUserIdentityDo::getIsPrimary, 0);
        if (excludeIdentityId != null) {
            update.ne(DefUserIdentityDo::getId, excludeIdentityId);
        }
        defUserIdentityMapper.update(null, update);
    }

    /**
     * 删除身份（逻辑删除）
     *
     * @param identityId 身份ID
     */
    @Override
    public void deleteIdentityById(Long identityId) {
        defUserIdentityMapper.deleteById(identityId);
    }

    /**
     * 根据用户ID查找第一个团队ID
     *
     * @param userId 用户ID
     * @return 团队ID
     */
    @Override
    public Long findFirstTeamId(Long userId) {
        return defTeamUserMapper.selectFirstTeamId(userId);
    }

    /**
     * 拥有活跃企业成员身份
     *
     * @param userId  用户ID
     * @param estabId 企业ID
     * @return 是否拥有活跃企业成员身份
     */
    @Override
    public boolean hasActiveEstabMembership(Long userId, Long estabId) {
        if (userId == null || estabId == null) {
            return false;
        }

        return defEstabUserMapper.selectActive(userId, estabId) != null;
    }

    /**
     * 是企业管理员
     *
     * @param userId  用户ID
     * @param estabId 企业ID
     * @return 是否是企业管理员
     */
    @Override
    public boolean isEstabAdmin(Long userId, Long estabId) {
        if (userId == null || estabId == null) {
            return false;
        }

        DefEstabUserDo estabUserDo = defEstabUserMapper.selectActive(userId, estabId);
        return estabUserDo != null && estabUserDo.getIsAdmin() != null && estabUserDo.getIsAdmin() == 1;
    }

    /**
     * 查询用户所属有效企业列表
     *
     * @param userId 用户ID
     * @return 企业列表
     */
    @Override
    public List<UserEstabEntity> listActiveUserEstabs(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        List<UserEstabJoinDo> rows = defEstabUserMapper.selectActiveEstabs(userId);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        return rows.stream().map(this::toUserEstabEntity).collect(Collectors.toList());
    }

    /**
     * 更新用户资料
     *
     * @param userId      用户ID
     * @param displayName 显示名称
     * @param nickname    昵称
     * @param avatarUrl   头像地址
     * @param gender      性别
     * @param birthday    生日
     */
    @Override
    public void updateUserProfile(Long userId, String displayName, String nickname, String avatarUrl, Integer gender, LocalDate birthday) {
        defUserMapper.update(
                null,
                Wrappers.lambdaUpdate(DefUserDo.class)
                        .eq(DefUserDo::getId, userId)
                        .set(DefUserDo::getDisplayName, displayName)
                        .set(DefUserDo::getNickname, nickname)
                        .set(DefUserDo::getAvatarUrl, avatarUrl)
                        .set(gender != null, DefUserDo::getGender, gender)
                        .set(DefUserDo::getBirthday, birthday)
        );
    }

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param avatarUrl 头像地址
     */
    @Override
    public void updateUserAvatar(Long userId, String avatarUrl) {
        defUserMapper.update(
                null,
                Wrappers.lambdaUpdate(DefUserDo.class)
                        .eq(DefUserDo::getId, userId)
                        .set(DefUserDo::getAvatarUrl, avatarUrl)
        );
    }

    /**
     * 标记登录成功
     *
     * @param userId    用户ID
     * @param identityId 身份ID
     * @param ip        登录IP
     * @param now       登录时间
     */
    @Override
    public void markLoginSuccess(Long userId, Long identityId, String ip, LocalDateTime now) {
        DefUserDo userDo = new DefUserDo();
        userDo.setId(userId);
        userDo.setLoginFailCount(0);
        userDo.setLastLoginIp(ip);
        userDo.setLastLoginTime(now);
        defUserMapper.updateById(userDo);

        DefUserIdentityDo identityDo = new DefUserIdentityDo();
        identityDo.setId(identityId);
        identityDo.setLastLoginIp(ip);
        identityDo.setLastLoginTime(now);
        defUserIdentityMapper.updateById(identityDo);
    }

    /**
     * 增加登录失败次数
     *
     * @param userId 用户ID
     * @return 登录失败次数
     */
    @Override
    public int incrementLoginFailCount(Long userId) {
        DefUserDo current = defUserMapper.selectById(userId);
        if (current == null) {
            return 0;
        }

        int failCount = current.getLoginFailCount() == null ? 0 : current.getLoginFailCount();
        failCount += 1;

        DefUserDo update = new DefUserDo();
        update.setId(userId);
        update.setLoginFailCount(failCount);
        defUserMapper.updateById(update);

        return failCount;
    }

    /**
     * 锁定用户
     *
     * @param userId     用户ID
     * @param lockUntil  锁定截止时间
     */
    @Override
    public void lockUser(Long userId, LocalDateTime lockUntil) {
        DefUserDo update = new DefUserDo();
        update.setId(userId);
        update.setLockUntil(lockUntil);
        update.setLoginFailCount(0);
        defUserMapper.updateById(update);
    }

    /**
     * 重置登录失败次数
     *
     * @param userId 用户ID
     */
    @Override
    public void resetLoginFailCount(Long userId) {
        DefUserDo update = new DefUserDo();
        update.setId(userId);
        update.setLoginFailCount(0);
        defUserMapper.updateById(update);
    }

    /**
     * 更新身份凭证
     *
     * @param identityId    身份ID
     * @param credential    新凭证（加密后）
     * @param credentialAlg 凭证算法
     * @param verified      是否已验证
     * @param verifiedAt    验证时间
     */
    @Override
    public void updateIdentityCredential(Long identityId, String credential, String credentialAlg, Integer verified, LocalDateTime verifiedAt) {
        DefUserIdentityDo update = new DefUserIdentityDo();
        update.setId(identityId);
        update.setCredential(credential);
        update.setCredentialAlg(credentialAlg);
        update.setVerified(verified);
        update.setVerifiedAt(verifiedAt);
        defUserIdentityMapper.updateById(update);
    }

    private UserEstabEntity toUserEstabEntity(UserEstabJoinDo row) {
        UserEstabEntity entity = new UserEstabEntity();
        entity.setEstabId(row.getEstabId());
        entity.setEstabCode(row.getEstabCode());
        entity.setEstabName(row.getEstabName());
        entity.setEstabShortName(row.getEstabShortName());
        entity.setLogoUrl(row.getLogoUrl());
        entity.setEstabType(row.getEstabType());
        entity.setIsAdmin(row.getIsAdmin());
        return entity;
    }
}
