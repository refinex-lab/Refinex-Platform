package cn.refinex.user.infrastructure.persistence.repository;

import cn.refinex.user.domain.model.entity.UserEntity;
import cn.refinex.user.domain.model.entity.UserIdentityEntity;
import cn.refinex.user.domain.repository.UserRepository;
import cn.refinex.user.infrastructure.converter.UserDoConverter;
import cn.refinex.user.infrastructure.converter.UserIdentityDoConverter;
import cn.refinex.user.infrastructure.persistence.dataobject.DefEstabDo;
import cn.refinex.user.infrastructure.persistence.dataobject.DefEstabUserDo;
import cn.refinex.user.infrastructure.persistence.dataobject.DefUserDo;
import cn.refinex.user.infrastructure.persistence.dataobject.DefUserIdentityDo;
import cn.refinex.user.infrastructure.persistence.mapper.DefEstabMapper;
import cn.refinex.user.infrastructure.persistence.mapper.DefEstabUserMapper;
import cn.refinex.user.infrastructure.persistence.mapper.DefTeamUserMapper;
import cn.refinex.user.infrastructure.persistence.mapper.DefUserIdentityMapper;
import cn.refinex.user.infrastructure.persistence.mapper.DefUserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

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
}
