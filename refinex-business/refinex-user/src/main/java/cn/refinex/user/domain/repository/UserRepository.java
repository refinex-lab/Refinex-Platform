package cn.refinex.user.domain.repository;

import cn.refinex.base.response.PageResponse;
import cn.refinex.user.domain.model.entity.UserEntity;
import cn.refinex.user.domain.model.entity.UserEstabEntity;
import cn.refinex.user.domain.model.entity.UserIdentityEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
     * 更新用户
     *
     * @param user 用户
     */
    void updateUser(UserEntity user);

    /**
     * 根据ID查找用户
     *
     * @param userId 用户ID
     * @return 用户
     */
    UserEntity findUserById(Long userId);

    /**
     * 查询用户管理列表
     *
     * @param primaryEstabId 主组织ID
     * @param status         用户状态
     * @param userType       用户类型
     * @param userCode       用户编码
     * @param username       用户名
     * @param displayName    显示名称
     * @param nickname       昵称
     * @param primaryPhone   主手机号
     * @param primaryEmail   主邮箱
     * @param keyword        关键字（兼容）
     * @param sortBy         排序字段
     * @param sortDirection  排序方向
     * @param userIds        用户ID列表
     * @param currentPage    当前页
     * @param pageSize       每页条数
     * @return 用户分页列表
     */
    PageResponse<UserEntity> listUsersForManage(Long primaryEstabId, Integer status, Integer userType,
                                                String userCode, String username, String displayName,
                                                String nickname, String primaryPhone, String primaryEmail,
                                                String keyword, String sortBy, String sortDirection,
                                                List<Long> userIds,
                                                int currentPage, int pageSize);

    /**
     * 批量查询企业名称映射
     *
     * @param estabIds 企业ID列表
     * @return 企业名称映射
     */
    Map<Long, String> listEstabNameMapByIds(List<Long> estabIds);

    /**
     * 根据用户编码统计用户数量
     *
     * @param userCode       用户编码
     * @param excludeUserId  排除用户ID
     * @return 用户数量
     */
    long countUserCode(String userCode, Long excludeUserId);

    /**
     * 根据用户名统计用户数量
     *
     * @param username       用户名
     * @param excludeUserId  排除用户ID
     * @return 用户数量
     */
    long countUsername(String username, Long excludeUserId);

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
     * 根据身份ID查询身份
     *
     * @param identityId 身份ID
     * @return 身份
     */
    UserIdentityEntity findIdentityById(Long identityId);

    /**
     * 查询用户身份列表
     *
     * @param userId 用户ID
     * @return 身份列表
     */
    List<UserIdentityEntity> listIdentitiesByUserId(Long userId);

    /**
     * 根据唯一键统计身份数量
     *
     * @param identityType       身份类型
     * @param identifier         标识符
     * @param issuer             发行方
     * @param excludeIdentityId  排除身份ID
     * @return 身份数量
     */
    long countIdentityByUnique(Integer identityType, String identifier, String issuer, Long excludeIdentityId);

    /**
     * 统计用户身份数量
     *
     * @param userId 用户ID
     * @return 身份数量
     */
    long countIdentityByUserId(Long userId);

    /**
     * 更新身份
     *
     * @param identity 身份
     */
    void updateIdentity(UserIdentityEntity identity);

    /**
     * 清理用户其他主身份标记
     *
     * @param userId            用户ID
     * @param excludeIdentityId 排除身份ID
     */
    void clearPrimaryIdentity(Long userId, Long excludeIdentityId);

    /**
     * 删除身份（逻辑删除）
     *
     * @param identityId 身份ID
     */
    void deleteIdentityById(Long identityId);

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
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param avatarUrl 头像地址
     */
    void updateUserAvatar(Long userId, String avatarUrl);

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
     * 逻辑删除用户
     *
     * @param userId 用户ID
     */
    void deleteUserById(Long userId);

    /**
     * 逻辑删除用户身份
     *
     * @param userId 用户ID
     */
    void deleteIdentityByUserId(Long userId);

    /**
     * 逻辑删除企业成员关系
     *
     * @param userId 用户ID
     */
    void deleteEstabUserRelationByUserId(Long userId);

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
