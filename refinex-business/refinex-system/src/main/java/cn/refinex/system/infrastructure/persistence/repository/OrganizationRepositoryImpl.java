package cn.refinex.system.infrastructure.persistence.repository;

import cn.refinex.base.response.PageResponse;
import cn.refinex.system.domain.model.entity.*;
import cn.refinex.system.domain.repository.OrganizationRepository;
import cn.refinex.system.infrastructure.persistence.dataobject.*;
import cn.refinex.system.infrastructure.persistence.mapper.*;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 企业与组织结构仓储实现
 *
 * @author refinex
 */
@Repository
@RequiredArgsConstructor
public class OrganizationRepositoryImpl implements OrganizationRepository {

    private final DefEstabMapper defEstabMapper;
    private final DefEstabAddressMapper defEstabAddressMapper;
    private final DefEstabAuthPolicyMapper defEstabAuthPolicyMapper;
    private final DefEstabUserMapper defEstabUserMapper;
    private final DefTeamMapper defTeamMapper;
    private final DefTeamUserMapper defTeamUserMapper;

    /**
     * 查询企业列表
     *
     * @param status    状态
     * @param estabType 企业类型
     * @param keyword   关键字
     * @return 企业列表
     */
    @Override
    public PageResponse<EstabEntity> listEstabs(Integer status, Integer estabType, String keyword,
                                                int currentPage, int pageSize) {
        var query = Wrappers.lambdaQuery(DefEstabDo.class)
                .eq(DefEstabDo::getDeleted, 0)
                .orderByAsc(DefEstabDo::getId);
        if (status != null) {
            query.eq(DefEstabDo::getStatus, status);
        }
        if (estabType != null) {
            query.eq(DefEstabDo::getEstabType, estabType);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(wrapper -> wrapper
                    .like(DefEstabDo::getEstabCode, trimmed)
                    .or()
                    .like(DefEstabDo::getEstabName, trimmed)
                    .or()
                    .like(DefEstabDo::getEstabShortName, trimmed));
        }
        Page<DefEstabDo> page = new Page<>(currentPage, pageSize);
        Page<DefEstabDo> rowsPage = defEstabMapper.selectPage(page, query);
        List<EstabEntity> list = rowsPage.getRecords().stream().map(this::toEstabEntity).collect(Collectors.toList());
        return PageResponse.of(list, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 查询企业
     *
     * @param estabId 企业ID
     * @return 企业
     */
    @Override
    public EstabEntity findEstabById(Long estabId) {
        DefEstabDo row = defEstabMapper.selectById(estabId);
        return row == null ? null : toEstabEntity(row);
    }

    /**
     * 企业编码去重统计
     *
     * @param estabCode      企业编码
     * @param excludeEstabId 排除企业ID
     * @return 数量
     */
    @Override
    public long countEstabCode(String estabCode, Long excludeEstabId) {
        var query = Wrappers.lambdaQuery(DefEstabDo.class)
                .eq(DefEstabDo::getEstabCode, estabCode)
                .eq(DefEstabDo::getDeleted, 0);
        if (excludeEstabId != null) {
            query.ne(DefEstabDo::getId, excludeEstabId);
        }
        Long count = defEstabMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 新增企业
     *
     * @param estab 企业
     * @return 企业
     */
    @Override
    public EstabEntity insertEstab(EstabEntity estab) {
        DefEstabDo row = toEstabDo(estab);
        defEstabMapper.insert(row);
        return toEstabEntity(row);
    }

    /**
     * 修改企业
     *
     * @param estab 企业
     */
    @Override
    public void updateEstab(EstabEntity estab) {
        DefEstabDo row = toEstabDo(estab);
        defEstabMapper.updateById(row);
    }

    /**
     * 删除企业（逻辑删除）
     *
     * @param estabId 企业ID
     */
    @Override
    public void deleteEstab(Long estabId) {
        defEstabMapper.deleteById(estabId);
    }

    /**
     * 企业成员数量
     *
     * @param estabId 企业ID
     * @return 成员数量
     */
    @Override
    public long countEstabUsers(Long estabId) {
        Long count = defEstabUserMapper.selectCount(
                Wrappers.lambdaQuery(DefEstabUserDo.class)
                        .eq(DefEstabUserDo::getEstabId, estabId)
                        .eq(DefEstabUserDo::getDeleted, 0)
        );
        return count == null ? 0L : count;
    }

    /**
     * 企业团队数量
     *
     * @param estabId 企业ID
     * @return 团队数量
     */
    @Override
    public long countEstabTeams(Long estabId) {
        Long count = defTeamMapper.selectCount(
                Wrappers.lambdaQuery(DefTeamDo.class)
                        .eq(DefTeamDo::getEstabId, estabId)
                        .eq(DefTeamDo::getDeleted, 0)
        );
        return count == null ? 0L : count;
    }

    /**
     * 查询企业地址列表
     *
     * @param estabId  企业ID
     * @param addrType 地址类型
     * @return 地址列表
     */
    @Override
    public PageResponse<EstabAddressEntity> listEstabAddresses(Long estabId, Integer addrType,
                                                               int currentPage, int pageSize) {
        var query = Wrappers.lambdaQuery(DefEstabAddressDo.class)
                .eq(DefEstabAddressDo::getEstabId, estabId)
                .eq(DefEstabAddressDo::getDeleted, 0)
                .orderByDesc(DefEstabAddressDo::getIsDefault)
                .orderByAsc(DefEstabAddressDo::getId);
        if (addrType != null) {
            query.eq(DefEstabAddressDo::getAddrType, addrType);
        }
        Page<DefEstabAddressDo> page = new Page<>(currentPage, pageSize);
        Page<DefEstabAddressDo> rowsPage = defEstabAddressMapper.selectPage(page, query);
        List<EstabAddressEntity> list = rowsPage.getRecords().stream().map(this::toEstabAddressEntity).collect(Collectors.toList());
        return PageResponse.of(list, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 查询企业地址
     *
     * @param addressId 地址ID
     * @return 地址
     */
    @Override
    public EstabAddressEntity findEstabAddressById(Long addressId) {
        DefEstabAddressDo row = defEstabAddressMapper.selectById(addressId);
        return row == null ? null : toEstabAddressEntity(row);
    }

    /**
     * 新增企业地址
     *
     * @param address 地址
     * @return 地址
     */
    @Override
    public EstabAddressEntity insertEstabAddress(EstabAddressEntity address) {
        DefEstabAddressDo row = toEstabAddressDo(address);
        defEstabAddressMapper.insert(row);
        return toEstabAddressEntity(row);
    }

    /**
     * 修改企业地址
     *
     * @param address 地址
     */
    @Override
    public void updateEstabAddress(EstabAddressEntity address) {
        DefEstabAddressDo row = toEstabAddressDo(address);
        defEstabAddressMapper.updateById(row);
    }

    /**
     * 删除企业地址
     *
     * @param addressId 地址ID
     */
    @Override
    public void deleteEstabAddress(Long addressId) {
        defEstabAddressMapper.deleteById(addressId);
    }

    /**
     * 清理默认地址标记
     *
     * @param estabId          企业ID
     * @param excludeAddressId 排除地址ID
     */
    @Override
    public void clearDefaultAddress(Long estabId, Long excludeAddressId) {
        var update = Wrappers.lambdaUpdate(DefEstabAddressDo.class)
                .eq(DefEstabAddressDo::getEstabId, estabId)
                .eq(DefEstabAddressDo::getDeleted, 0)
                .set(DefEstabAddressDo::getIsDefault, 0);
        if (excludeAddressId != null) {
            update.ne(DefEstabAddressDo::getId, excludeAddressId);
        }
        defEstabAddressMapper.update(null, update);
    }

    /**
     * 查询企业认证策略
     *
     * @param estabId 企业ID
     * @return 策略
     */
    @Override
    public EstabAuthPolicyEntity findEstabAuthPolicy(Long estabId) {
        DefEstabAuthPolicyDo row = defEstabAuthPolicyMapper.selectOne(
                Wrappers.lambdaQuery(DefEstabAuthPolicyDo.class)
                        .eq(DefEstabAuthPolicyDo::getEstabId, estabId)
                        .eq(DefEstabAuthPolicyDo::getDeleted, 0)
                        .last("LIMIT 1")
        );
        return row == null ? null : toEstabAuthPolicyEntity(row);
    }

    /**
     * 保存企业认证策略
     *
     * @param policy 策略
     * @return 策略
     */
    @Override
    public EstabAuthPolicyEntity saveEstabAuthPolicy(EstabAuthPolicyEntity policy) {
        if (policy.getId() == null) {
            DefEstabAuthPolicyDo row = toEstabAuthPolicyDo(policy);
            defEstabAuthPolicyMapper.insert(row);
            return toEstabAuthPolicyEntity(row);
        }
        DefEstabAuthPolicyDo row = toEstabAuthPolicyDo(policy);
        defEstabAuthPolicyMapper.updateById(row);
        return toEstabAuthPolicyEntity(defEstabAuthPolicyMapper.selectById(policy.getId()));
    }

    /**
     * 查询企业成员列表
     *
     * @param estabId 企业ID
     * @param status  状态
     * @return 成员列表
     */
    @Override
    public PageResponse<EstabUserEntity> listEstabUsers(Long estabId, Integer status, int currentPage, int pageSize) {
        var query = Wrappers.lambdaQuery(DefEstabUserDo.class)
                .eq(DefEstabUserDo::getEstabId, estabId)
                .eq(DefEstabUserDo::getDeleted, 0)
                .orderByDesc(DefEstabUserDo::getIsAdmin)
                .orderByAsc(DefEstabUserDo::getId);
        if (status != null) {
            query.eq(DefEstabUserDo::getStatus, status);
        }
        Page<DefEstabUserDo> page = new Page<>(currentPage, pageSize);
        Page<DefEstabUserDo> rowsPage = defEstabUserMapper.selectPage(page, query);
        List<EstabUserEntity> list = rowsPage.getRecords().stream().map(this::toEstabUserEntity).collect(Collectors.toList());
        return PageResponse.of(list, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 查询企业成员关系
     *
     * @param estabUserId 企业成员关系ID
     * @return 成员关系
     */
    @Override
    public EstabUserEntity findEstabUserById(Long estabUserId) {
        DefEstabUserDo row = defEstabUserMapper.selectById(estabUserId);
        return row == null ? null : toEstabUserEntity(row);
    }

    /**
     * 成员关系去重统计
     *
     * @param estabId            企业ID
     * @param userId             用户ID
     * @param excludeEstabUserId 排除关系ID
     * @return 关系数量
     */
    @Override
    public long countEstabUserRelation(Long estabId, Long userId, Long excludeEstabUserId) {
        var query = Wrappers.lambdaQuery(DefEstabUserDo.class)
                .eq(DefEstabUserDo::getEstabId, estabId)
                .eq(DefEstabUserDo::getUserId, userId)
                .eq(DefEstabUserDo::getDeleted, 0);
        if (excludeEstabUserId != null) {
            query.ne(DefEstabUserDo::getId, excludeEstabUserId);
        }
        Long count = defEstabUserMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 新增企业成员关系
     *
     * @param estabUser 成员关系
     * @return 成员关系
     */
    @Override
    public EstabUserEntity insertEstabUser(EstabUserEntity estabUser) {
        DefEstabUserDo row = toEstabUserDo(estabUser);
        defEstabUserMapper.insert(row);
        return toEstabUserEntity(row);
    }

    /**
     * 修改企业成员关系
     *
     * @param estabUser 成员关系
     */
    @Override
    public void updateEstabUser(EstabUserEntity estabUser) {
        DefEstabUserDo row = toEstabUserDo(estabUser);
        defEstabUserMapper.updateById(row);
    }

    /**
     * 删除企业成员关系
     *
     * @param estabUserId 企业成员关系ID
     */
    @Override
    public void deleteEstabUser(Long estabUserId) {
        defEstabUserMapper.deleteById(estabUserId);
    }

    /**
     * 查询团队列表
     *
     * @param estabId  企业ID
     * @param parentId 父团队ID
     * @param status   状态
     * @param keyword  关键字
     * @return 团队列表
     */
    @Override
    public PageResponse<TeamEntity> listTeams(Long estabId, Long parentId, Integer status, String keyword,
                                              int currentPage, int pageSize) {
        var query = Wrappers.lambdaQuery(DefTeamDo.class)
                .eq(DefTeamDo::getDeleted, 0)
                .orderByAsc(DefTeamDo::getSort, DefTeamDo::getId);
        if (estabId != null) {
            query.eq(DefTeamDo::getEstabId, estabId);
        }
        if (parentId != null) {
            query.eq(DefTeamDo::getParentId, parentId);
        }
        if (status != null) {
            query.eq(DefTeamDo::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(wrapper -> wrapper
                    .like(DefTeamDo::getTeamCode, trimmed)
                    .or()
                    .like(DefTeamDo::getTeamName, trimmed));
        }
        Page<DefTeamDo> page = new Page<>(currentPage, pageSize);
        Page<DefTeamDo> rowsPage = defTeamMapper.selectPage(page, query);
        List<TeamEntity> list = rowsPage.getRecords().stream().map(this::toTeamEntity).collect(Collectors.toList());
        return PageResponse.of(list, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 查询团队
     *
     * @param teamId 团队ID
     * @return 团队
     */
    @Override
    public TeamEntity findTeamById(Long teamId) {
        DefTeamDo row = defTeamMapper.selectById(teamId);
        return row == null ? null : toTeamEntity(row);
    }

    /**
     * 团队编码去重统计
     *
     * @param estabId       企业ID
     * @param teamCode      团队编码
     * @param excludeTeamId 排除团队ID
     * @return 数量
     */
    @Override
    public long countTeamCode(Long estabId, String teamCode, Long excludeTeamId) {
        var query = Wrappers.lambdaQuery(DefTeamDo.class)
                .eq(DefTeamDo::getEstabId, estabId)
                .eq(DefTeamDo::getTeamCode, teamCode)
                .eq(DefTeamDo::getDeleted, 0);
        if (excludeTeamId != null) {
            query.ne(DefTeamDo::getId, excludeTeamId);
        }
        Long count = defTeamMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 新增团队
     *
     * @param team 团队
     * @return 团队
     */
    @Override
    public TeamEntity insertTeam(TeamEntity team) {
        DefTeamDo row = toTeamDo(team);
        defTeamMapper.insert(row);
        return toTeamEntity(row);
    }

    /**
     * 修改团队
     *
     * @param team 团队
     */
    @Override
    public void updateTeam(TeamEntity team) {
        DefTeamDo row = toTeamDo(team);
        defTeamMapper.updateById(row);
    }

    /**
     * 删除团队
     *
     * @param teamId 团队ID
     */
    @Override
    public void deleteTeam(Long teamId) {
        defTeamMapper.deleteById(teamId);
    }

    /**
     * 子团队数量
     *
     * @param teamId 团队ID
     * @return 子团队数量
     */
    @Override
    public long countChildTeams(Long teamId) {
        Long count = defTeamMapper.selectCount(
                Wrappers.lambdaQuery(DefTeamDo.class)
                        .eq(DefTeamDo::getParentId, teamId)
                        .eq(DefTeamDo::getDeleted, 0)
        );
        return count == null ? 0L : count;
    }

    /**
     * 团队成员数量
     *
     * @param teamId 团队ID
     * @return 团队成员数量
     */
    @Override
    public long countTeamUsers(Long teamId) {
        Long count = defTeamUserMapper.selectCount(
                Wrappers.lambdaQuery(DefTeamUserDo.class)
                        .eq(DefTeamUserDo::getTeamId, teamId)
                        .eq(DefTeamUserDo::getDeleted, 0)
        );
        return count == null ? 0L : count;
    }

    /**
     * 查询团队成员列表
     *
     * @param teamId 团队ID
     * @param status 状态
     * @return 团队成员列表
     */
    @Override
    public PageResponse<TeamUserEntity> listTeamUsers(Long teamId, Integer status, int currentPage, int pageSize) {
        var query = Wrappers.lambdaQuery(DefTeamUserDo.class)
                .eq(DefTeamUserDo::getTeamId, teamId)
                .eq(DefTeamUserDo::getDeleted, 0)
                .orderByDesc(DefTeamUserDo::getRoleInTeam)
                .orderByAsc(DefTeamUserDo::getId);
        if (status != null) {
            query.eq(DefTeamUserDo::getStatus, status);
        }
        Page<DefTeamUserDo> page = new Page<>(currentPage, pageSize);
        Page<DefTeamUserDo> rowsPage = defTeamUserMapper.selectPage(page, query);
        List<TeamUserEntity> list = rowsPage.getRecords().stream().map(this::toTeamUserEntity).collect(Collectors.toList());
        return PageResponse.of(list, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 查询团队成员关系
     *
     * @param teamUserId 团队成员关系ID
     * @return 团队成员关系
     */
    @Override
    public TeamUserEntity findTeamUserById(Long teamUserId) {
        DefTeamUserDo row = defTeamUserMapper.selectById(teamUserId);
        return row == null ? null : toTeamUserEntity(row);
    }

    /**
     * 团队成员关系去重统计
     *
     * @param teamId            团队ID
     * @param userId            用户ID
     * @param excludeTeamUserId 排除关系ID
     * @return 数量
     */
    @Override
    public long countTeamUserRelation(Long teamId, Long userId, Long excludeTeamUserId) {
        var query = Wrappers.lambdaQuery(DefTeamUserDo.class)
                .eq(DefTeamUserDo::getTeamId, teamId)
                .eq(DefTeamUserDo::getUserId, userId)
                .eq(DefTeamUserDo::getDeleted, 0);
        if (excludeTeamUserId != null) {
            query.ne(DefTeamUserDo::getId, excludeTeamUserId);
        }
        Long count = defTeamUserMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 新增团队成员关系
     *
     * @param teamUser 团队成员关系
     * @return 团队成员关系
     */
    @Override
    public TeamUserEntity insertTeamUser(TeamUserEntity teamUser) {
        DefTeamUserDo row = toTeamUserDo(teamUser);
        defTeamUserMapper.insert(row);
        return toTeamUserEntity(row);
    }

    /**
     * 修改团队成员关系
     *
     * @param teamUser 团队成员关系
     */
    @Override
    public void updateTeamUser(TeamUserEntity teamUser) {
        DefTeamUserDo row = toTeamUserDo(teamUser);
        defTeamUserMapper.updateById(row);
    }

    /**
     * 删除团队成员关系
     *
     * @param teamUserId 团队成员关系ID
     */
    @Override
    public void deleteTeamUser(Long teamUserId) {
        defTeamUserMapper.deleteById(teamUserId);
    }

    private EstabEntity toEstabEntity(DefEstabDo row) {
        EstabEntity entity = new EstabEntity();
        entity.setId(row.getId());
        entity.setEstabCode(row.getEstabCode());
        entity.setEstabName(row.getEstabName());
        entity.setEstabShortName(row.getEstabShortName());
        entity.setEstabType(row.getEstabType());
        entity.setStatus(row.getStatus());
        entity.setIndustryCode(row.getIndustryCode());
        entity.setSizeRange(row.getSizeRange());
        entity.setOwnerUserId(row.getOwnerUserId());
        entity.setContactName(row.getContactName());
        entity.setContactPhone(row.getContactPhone());
        entity.setContactEmail(row.getContactEmail());
        entity.setWebsiteUrl(row.getWebsiteUrl());
        entity.setLogoUrl(row.getLogoUrl());
        entity.setRemark(row.getRemark());
        entity.setDeleted(row.getDeleted());
        return entity;
    }

    private DefEstabDo toEstabDo(EstabEntity entity) {
        DefEstabDo row = new DefEstabDo();
        row.setId(entity.getId());
        row.setEstabCode(entity.getEstabCode());
        row.setEstabName(entity.getEstabName());
        row.setEstabShortName(entity.getEstabShortName());
        row.setEstabType(entity.getEstabType());
        row.setStatus(entity.getStatus());
        row.setIndustryCode(entity.getIndustryCode());
        row.setSizeRange(entity.getSizeRange());
        row.setOwnerUserId(entity.getOwnerUserId());
        row.setContactName(entity.getContactName());
        row.setContactPhone(entity.getContactPhone());
        row.setContactEmail(entity.getContactEmail());
        row.setWebsiteUrl(entity.getWebsiteUrl());
        row.setLogoUrl(entity.getLogoUrl());
        row.setRemark(entity.getRemark());
        return row;
    }

    private EstabAddressEntity toEstabAddressEntity(DefEstabAddressDo row) {
        EstabAddressEntity entity = new EstabAddressEntity();
        entity.setId(row.getId());
        entity.setEstabId(row.getEstabId());
        entity.setAddrType(row.getAddrType());
        entity.setCountryCode(row.getCountryCode());
        entity.setProvinceCode(row.getProvinceCode());
        entity.setCityCode(row.getCityCode());
        entity.setDistrictCode(row.getDistrictCode());
        entity.setProvinceName(row.getProvinceName());
        entity.setCityName(row.getCityName());
        entity.setDistrictName(row.getDistrictName());
        entity.setAddressLine1(row.getAddressLine1());
        entity.setAddressLine2(row.getAddressLine2());
        entity.setPostalCode(row.getPostalCode());
        entity.setLatitude(row.getLatitude());
        entity.setLongitude(row.getLongitude());
        entity.setIsDefault(row.getIsDefault());
        entity.setRemark(row.getRemark());
        entity.setDeleted(row.getDeleted());
        return entity;
    }

    private DefEstabAddressDo toEstabAddressDo(EstabAddressEntity entity) {
        DefEstabAddressDo row = new DefEstabAddressDo();
        row.setId(entity.getId());
        row.setEstabId(entity.getEstabId());
        row.setAddrType(entity.getAddrType());
        row.setCountryCode(entity.getCountryCode());
        row.setProvinceCode(entity.getProvinceCode());
        row.setCityCode(entity.getCityCode());
        row.setDistrictCode(entity.getDistrictCode());
        row.setProvinceName(entity.getProvinceName());
        row.setCityName(entity.getCityName());
        row.setDistrictName(entity.getDistrictName());
        row.setAddressLine1(entity.getAddressLine1());
        row.setAddressLine2(entity.getAddressLine2());
        row.setPostalCode(entity.getPostalCode());
        row.setLatitude(entity.getLatitude());
        row.setLongitude(entity.getLongitude());
        row.setIsDefault(entity.getIsDefault());
        row.setRemark(entity.getRemark());
        return row;
    }

    private EstabAuthPolicyEntity toEstabAuthPolicyEntity(DefEstabAuthPolicyDo row) {
        EstabAuthPolicyEntity entity = new EstabAuthPolicyEntity();
        entity.setId(row.getId());
        entity.setEstabId(row.getEstabId());
        entity.setPasswordLoginEnabled(row.getPasswordLoginEnabled());
        entity.setSmsLoginEnabled(row.getSmsLoginEnabled());
        entity.setEmailLoginEnabled(row.getEmailLoginEnabled());
        entity.setWechatLoginEnabled(row.getWechatLoginEnabled());
        entity.setMfaRequired(row.getMfaRequired());
        entity.setMfaMethods(row.getMfaMethods());
        entity.setPasswordMinLen(row.getPasswordMinLen());
        entity.setPasswordStrength(row.getPasswordStrength());
        entity.setPasswordExpireDays(row.getPasswordExpireDays());
        entity.setLoginFailThreshold(row.getLoginFailThreshold());
        entity.setLockMinutes(row.getLockMinutes());
        entity.setSessionTimeoutMinutes(row.getSessionTimeoutMinutes());
        entity.setRemark(row.getRemark());
        entity.setDeleted(row.getDeleted());
        return entity;
    }

    private DefEstabAuthPolicyDo toEstabAuthPolicyDo(EstabAuthPolicyEntity entity) {
        DefEstabAuthPolicyDo row = new DefEstabAuthPolicyDo();
        row.setId(entity.getId());
        row.setEstabId(entity.getEstabId());
        row.setPasswordLoginEnabled(entity.getPasswordLoginEnabled());
        row.setSmsLoginEnabled(entity.getSmsLoginEnabled());
        row.setEmailLoginEnabled(entity.getEmailLoginEnabled());
        row.setWechatLoginEnabled(entity.getWechatLoginEnabled());
        row.setMfaRequired(entity.getMfaRequired());
        row.setMfaMethods(entity.getMfaMethods());
        row.setPasswordMinLen(entity.getPasswordMinLen());
        row.setPasswordStrength(entity.getPasswordStrength());
        row.setPasswordExpireDays(entity.getPasswordExpireDays());
        row.setLoginFailThreshold(entity.getLoginFailThreshold());
        row.setLockMinutes(entity.getLockMinutes());
        row.setSessionTimeoutMinutes(entity.getSessionTimeoutMinutes());
        row.setRemark(entity.getRemark());
        return row;
    }

    private EstabUserEntity toEstabUserEntity(DefEstabUserDo row) {
        EstabUserEntity entity = new EstabUserEntity();
        entity.setId(row.getId());
        entity.setEstabId(row.getEstabId());
        entity.setUserId(row.getUserId());
        entity.setMemberType(row.getMemberType());
        entity.setIsAdmin(row.getIsAdmin());
        entity.setStatus(row.getStatus());
        entity.setJoinTime(row.getJoinTime());
        entity.setLeaveTime(row.getLeaveTime());
        entity.setPositionTitle(row.getPositionTitle());
        entity.setDeleted(row.getDeleted());
        return entity;
    }

    private DefEstabUserDo toEstabUserDo(EstabUserEntity entity) {
        DefEstabUserDo row = new DefEstabUserDo();
        row.setId(entity.getId());
        row.setEstabId(entity.getEstabId());
        row.setUserId(entity.getUserId());
        row.setMemberType(entity.getMemberType());
        row.setIsAdmin(entity.getIsAdmin());
        row.setStatus(entity.getStatus());
        row.setJoinTime(entity.getJoinTime());
        row.setLeaveTime(entity.getLeaveTime());
        row.setPositionTitle(entity.getPositionTitle());
        return row;
    }

    private TeamEntity toTeamEntity(DefTeamDo row) {
        TeamEntity entity = new TeamEntity();
        entity.setId(row.getId());
        entity.setEstabId(row.getEstabId());
        entity.setTeamCode(row.getTeamCode());
        entity.setTeamName(row.getTeamName());
        entity.setParentId(row.getParentId());
        entity.setLeaderUserId(row.getLeaderUserId());
        entity.setStatus(row.getStatus());
        entity.setSort(row.getSort());
        entity.setRemark(row.getRemark());
        entity.setDeleted(row.getDeleted());
        return entity;
    }

    private DefTeamDo toTeamDo(TeamEntity entity) {
        DefTeamDo row = new DefTeamDo();
        row.setId(entity.getId());
        row.setEstabId(entity.getEstabId());
        row.setTeamCode(entity.getTeamCode());
        row.setTeamName(entity.getTeamName());
        row.setParentId(entity.getParentId());
        row.setLeaderUserId(entity.getLeaderUserId());
        row.setStatus(entity.getStatus());
        row.setSort(entity.getSort());
        row.setRemark(entity.getRemark());
        return row;
    }

    private TeamUserEntity toTeamUserEntity(DefTeamUserDo row) {
        TeamUserEntity entity = new TeamUserEntity();
        entity.setId(row.getId());
        entity.setTeamId(row.getTeamId());
        entity.setUserId(row.getUserId());
        entity.setRoleInTeam(row.getRoleInTeam());
        entity.setStatus(row.getStatus());
        entity.setJoinTime(row.getJoinTime());
        entity.setDeleted(row.getDeleted());
        return entity;
    }

    private DefTeamUserDo toTeamUserDo(TeamUserEntity entity) {
        DefTeamUserDo row = new DefTeamUserDo();
        row.setId(entity.getId());
        row.setTeamId(entity.getTeamId());
        row.setUserId(entity.getUserId());
        row.setRoleInTeam(entity.getRoleInTeam());
        row.setStatus(entity.getStatus());
        row.setJoinTime(entity.getJoinTime());
        return row;
    }
}
