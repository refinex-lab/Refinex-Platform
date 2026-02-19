package cn.refinex.system.domain.repository;

import cn.refinex.system.domain.model.entity.*;

import java.util.List;

/**
 * 企业与组织结构仓储
 *
 * @author refinex
 */
public interface OrganizationRepository {

    /**
     * 查询企业列表
     *
     * @param status    状态
     * @param estabType 企业类型
     * @param keyword   关键字
     * @return 企业列表
     */
    List<EstabEntity> listEstabs(Integer status, Integer estabType, String keyword);

    /**
     * 查询企业
     *
     * @param estabId 企业ID
     * @return 企业
     */
    EstabEntity findEstabById(Long estabId);

    /**
     * 企业编码去重统计
     *
     * @param estabCode      企业编码
     * @param excludeEstabId 排除企业ID
     * @return 数量
     */
    long countEstabCode(String estabCode, Long excludeEstabId);

    /**
     * 新增企业
     *
     * @param estab 企业
     * @return 企业
     */
    EstabEntity insertEstab(EstabEntity estab);

    /**
     * 修改企业
     *
     * @param estab 企业
     */
    void updateEstab(EstabEntity estab);

    /**
     * 删除企业（逻辑删除）
     *
     * @param estabId 企业ID
     */
    void deleteEstab(Long estabId);

    /**
     * 企业成员数量
     *
     * @param estabId 企业ID
     * @return 成员数量
     */
    long countEstabUsers(Long estabId);

    /**
     * 企业团队数量
     *
     * @param estabId 企业ID
     * @return 团队数量
     */
    long countEstabTeams(Long estabId);

    /**
     * 查询企业地址列表
     *
     * @param estabId  企业ID
     * @param addrType 地址类型
     * @return 地址列表
     */
    List<EstabAddressEntity> listEstabAddresses(Long estabId, Integer addrType);

    /**
     * 查询企业地址
     *
     * @param addressId 地址ID
     * @return 地址
     */
    EstabAddressEntity findEstabAddressById(Long addressId);

    /**
     * 新增企业地址
     *
     * @param address 地址
     * @return 地址
     */
    EstabAddressEntity insertEstabAddress(EstabAddressEntity address);

    /**
     * 修改企业地址
     *
     * @param address 地址
     */
    void updateEstabAddress(EstabAddressEntity address);

    /**
     * 删除企业地址（逻辑删除）
     *
     * @param addressId 地址ID
     */
    void deleteEstabAddress(Long addressId);

    /**
     * 清理默认地址标记
     *
     * @param estabId           企业ID
     * @param excludeAddressId  排除地址ID
     */
    void clearDefaultAddress(Long estabId, Long excludeAddressId);

    /**
     * 查询企业认证策略
     *
     * @param estabId 企业ID
     * @return 策略
     */
    EstabAuthPolicyEntity findEstabAuthPolicy(Long estabId);

    /**
     * 保存企业认证策略
     *
     * @param policy 策略
     * @return 策略
     */
    EstabAuthPolicyEntity saveEstabAuthPolicy(EstabAuthPolicyEntity policy);

    /**
     * 查询企业成员列表
     *
     * @param estabId 企业ID
     * @param status  状态
     * @return 成员列表
     */
    List<EstabUserEntity> listEstabUsers(Long estabId, Integer status);

    /**
     * 查询企业成员关系
     *
     * @param estabUserId 企业成员关系ID
     * @return 成员关系
     */
    EstabUserEntity findEstabUserById(Long estabUserId);

    /**
     * 成员关系去重统计
     *
     * @param estabId             企业ID
     * @param userId              用户ID
     * @param excludeEstabUserId  排除关系ID
     * @return 关系数量
     */
    long countEstabUserRelation(Long estabId, Long userId, Long excludeEstabUserId);

    /**
     * 新增企业成员关系
     *
     * @param estabUser 成员关系
     * @return 成员关系
     */
    EstabUserEntity insertEstabUser(EstabUserEntity estabUser);

    /**
     * 修改企业成员关系
     *
     * @param estabUser 成员关系
     */
    void updateEstabUser(EstabUserEntity estabUser);

    /**
     * 删除企业成员关系（逻辑删除）
     *
     * @param estabUserId 企业成员关系ID
     */
    void deleteEstabUser(Long estabUserId);

    /**
     * 查询团队列表
     *
     * @param estabId   企业ID
     * @param parentId  父团队ID
     * @param status    状态
     * @param keyword   关键字
     * @return 团队列表
     */
    List<TeamEntity> listTeams(Long estabId, Long parentId, Integer status, String keyword);

    /**
     * 查询团队
     *
     * @param teamId 团队ID
     * @return 团队
     */
    TeamEntity findTeamById(Long teamId);

    /**
     * 团队编码去重统计
     *
     * @param estabId        企业ID
     * @param teamCode       团队编码
     * @param excludeTeamId  排除团队ID
     * @return 数量
     */
    long countTeamCode(Long estabId, String teamCode, Long excludeTeamId);

    /**
     * 新增团队
     *
     * @param team 团队
     * @return 团队
     */
    TeamEntity insertTeam(TeamEntity team);

    /**
     * 修改团队
     *
     * @param team 团队
     */
    void updateTeam(TeamEntity team);

    /**
     * 删除团队（逻辑删除）
     *
     * @param teamId 团队ID
     */
    void deleteTeam(Long teamId);

    /**
     * 子团队数量
     *
     * @param teamId 团队ID
     * @return 子团队数量
     */
    long countChildTeams(Long teamId);

    /**
     * 团队成员数量
     *
     * @param teamId 团队ID
     * @return 成员数量
     */
    long countTeamUsers(Long teamId);

    /**
     * 查询团队成员列表
     *
     * @param teamId 团队ID
     * @param status 状态
     * @return 团队成员列表
     */
    List<TeamUserEntity> listTeamUsers(Long teamId, Integer status);

    /**
     * 查询团队成员关系
     *
     * @param teamUserId 团队成员关系ID
     * @return 团队成员关系
     */
    TeamUserEntity findTeamUserById(Long teamUserId);

    /**
     * 团队成员关系去重统计
     *
     * @param teamId            团队ID
     * @param userId            用户ID
     * @param excludeTeamUserId 排除关系ID
     * @return 数量
     */
    long countTeamUserRelation(Long teamId, Long userId, Long excludeTeamUserId);

    /**
     * 新增团队成员关系
     *
     * @param teamUser 团队成员关系
     * @return 团队成员关系
     */
    TeamUserEntity insertTeamUser(TeamUserEntity teamUser);

    /**
     * 修改团队成员关系
     *
     * @param teamUser 团队成员关系
     */
    void updateTeamUser(TeamUserEntity teamUser);

    /**
     * 删除团队成员关系（逻辑删除）
     *
     * @param teamUserId 团队成员关系ID
     */
    void deleteTeamUser(Long teamUserId);
}
