package cn.refinex.user.domain.repository;

import cn.refinex.user.domain.model.entity.UserEntity;
import cn.refinex.user.domain.model.entity.UserEstabEntity;
import cn.refinex.user.domain.model.entity.UserIdentityEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户仓储
 *
 * @author refinex
 */
public interface UserRepository {

    /**
     * 统计身份
     *
     * @param identityType 身份类型
     * @param identifier   标识符
     * @return 身份数量
     */
    long countIdentity(Integer identityType, String identifier);

    /**
     * 插入用户
     *
     * @param user 用户
     * @return 用户
     */
    UserEntity insertUser(UserEntity user);

    /**
     * 根据ID查找用户
     *
     * @param userId 用户ID
     * @return 用户
     */
    UserEntity findUserById(Long userId);

    /**
     * 更新用户主企业
     *
     * @param userId  用户ID
     * @param estabId 企业ID
     */
    void updateUserPrimaryEstab(Long userId, Long estabId);

    /**
     * 插入企业
     *
     * @param estabCode   企业代码
     * @param estabName   企业名称
     * @param ownerUserId 企业所有者用户ID
     * @return 企业ID
     */
    Long insertEstab(String estabCode, String estabName, Long ownerUserId);

    /**
     * 根据企业ID和企业代码查找企业
     *
     * @param estabId   企业ID
     * @param estabCode 企业代码
     * @return 企业ID
     */
    Long findEstabId(Long estabId, String estabCode);

    /**
     * 插入企业用户关系
     *
     * @param estabId   企业ID
     * @param userId    用户ID
     * @param isAdmin   是否管理员
     * @param joinTime  加入时间
     */
    void insertEstabUserRelation(Long estabId, Long userId, Integer isAdmin, LocalDateTime joinTime);

    /**
     * 插入身份
     *
     * @param identity 身份
     * @return 身份
     */
    UserIdentityEntity insertIdentity(UserIdentityEntity identity);

    /**
     * 根据类型和标识符查找身份
     *
     * @param identityType 身份类型
     * @param identifier   标识符
     * @return 身份
     */
    UserIdentityEntity findIdentityByTypeAndIdentifier(Integer identityType, String identifier);

    /**
     * 根据用户ID和类型查找身份
     *
     * @param userId       用户ID
     * @param identityType 身份类型
     * @return 身份
     */
    UserIdentityEntity findIdentityByUserIdAndType(Long userId, Integer identityType);

    /**
     * 根据用户ID查找第一个企业
     *
     * @param userId 用户ID
     * @return 企业ID
     */
    Long findFirstTeamId(Long userId);

    /**
     * 用户是否在企业中
     *
     * @param userId  用户ID
     * @param estabId 企业ID
     * @return 用户是否在企业中
     */
    boolean hasActiveEstabMembership(Long userId, Long estabId);

    /**
     * 用户是否是企业管理员
     *
     * @param userId  用户ID
     * @param estabId 企业ID
     * @return 用户是否是企业管理员
     */
    boolean isEstabAdmin(Long userId, Long estabId);

    /**
     * 查询用户所属有效企业列表
     *
     * @param userId 用户ID
     * @return 企业列表
     */
    List<UserEstabEntity> listActiveUserEstabs(Long userId);

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
    void updateUserProfile(Long userId, String displayName, String nickname, String avatarUrl, Integer gender, LocalDate birthday);

    /**
     * 标记登录成功
     *
     * @param userId    用户ID
     * @param identityId 身份ID
     * @param ip         IP地址
     * @param now        现在
     */
    void markLoginSuccess(Long userId, Long identityId, String ip, LocalDateTime now);

    /**
     * 增加登录失败次数
     *
     * @param userId 用户ID
     * @return 登录失败次数
     */
    int incrementLoginFailCount(Long userId);

    /**
     * 锁定用户
     *
     * @param userId     用户ID
     * @param lockUntil  锁定到
     */
    void lockUser(Long userId, LocalDateTime lockUntil);

    /**
     * 重置登录失败次数
     *
     * @param userId 用户ID
     */
    void resetLoginFailCount(Long userId);

    /**
     * 更新身份凭证
     *
     * @param identityId    身份ID
     * @param credential    新凭证（加密后）
     * @param credentialAlg 凭证算法
     * @param verified      是否已验证
     * @param verifiedAt    验证时间
     */
    void updateIdentityCredential(Long identityId, String credential, String credentialAlg, Integer verified, LocalDateTime verifiedAt);
}
