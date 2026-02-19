package cn.refinex.system.application.service;

import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.PageResponse;
import cn.refinex.base.utils.PageUtils;
import cn.refinex.system.application.command.*;
import cn.refinex.system.application.dto.*;
import cn.refinex.system.domain.error.SystemErrorCode;
import cn.refinex.system.domain.model.entity.*;
import cn.refinex.system.domain.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.refinex.base.utils.ValueUtils.defaultIfNull;
import static cn.refinex.base.utils.ValueUtils.isBlank;
import static cn.refinex.base.utils.ValueUtils.trimToNull;

/**
 * 企业与组织结构应用服务
 *
 * @author refinex
 */
@Service
@RequiredArgsConstructor
public class OrganizationApplicationService {

    private final OrganizationRepository organizationRepository;

    /**
     * 查询企业列表
     *
     * @param command 查询命令
     * @return 企业列表
     */
    public PageResponse<EstabDTO> listEstabs(QueryEstabListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);
        PageResponse<EstabEntity> entities = organizationRepository.listEstabs(
                command == null ? null : command.getStatus(),
                command == null ? null : command.getEstabType(),
                command == null ? null : command.getKeyword(),
                currentPage,
                pageSize
        );
        List<EstabDTO> result = new ArrayList<>();
        for (EstabEntity entity : entities.getData()) {
            result.add(toEstabDto(entity));
        }
        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询企业详情
     *
     * @param estabId 企业ID
     * @return 企业详情
     */
    public EstabDTO getEstab(Long estabId) {
        return toEstabDto(requireEstab(estabId));
    }

    /**
     * 创建企业
     *
     * @param command 创建命令
     * @return 企业详情
     */
    @Transactional(rollbackFor = Exception.class)
    public EstabDTO createEstab(CreateEstabCommand command) {
        if (command == null || isBlank(command.getEstabCode()) || isBlank(command.getEstabName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        String estabCode = command.getEstabCode().trim();
        if (organizationRepository.countEstabCode(estabCode, null) > 0) {
            throw new BizException(SystemErrorCode.ESTAB_CODE_DUPLICATED);
        }

        EstabEntity estab = new EstabEntity();
        estab.setEstabCode(estabCode);
        estab.setEstabName(command.getEstabName().trim());
        estab.setEstabShortName(trimToNull(command.getEstabShortName()));
        estab.setEstabType(defaultIfNull(command.getEstabType(), 1));
        estab.setStatus(defaultIfNull(command.getStatus(), 1));
        estab.setIndustryCode(trimToNull(command.getIndustryCode()));
        estab.setSizeRange(trimToNull(command.getSizeRange()));
        estab.setOwnerUserId(command.getOwnerUserId());
        estab.setContactName(trimToNull(command.getContactName()));
        estab.setContactPhone(trimToNull(command.getContactPhone()));
        estab.setContactEmail(trimToNull(command.getContactEmail()));
        estab.setWebsiteUrl(trimToNull(command.getWebsiteUrl()));
        estab.setLogoUrl(trimToNull(command.getLogoUrl()));
        estab.setRemark(trimToNull(command.getRemark()));

        EstabEntity created = organizationRepository.insertEstab(estab);
        return toEstabDto(created);
    }

    /**
     * 更新企业
     *
     * @param command 更新命令
     * @return 企业详情
     */
    @Transactional(rollbackFor = Exception.class)
    public EstabDTO updateEstab(UpdateEstabCommand command) {
        if (command == null || command.getEstabId() == null || isBlank(command.getEstabName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        EstabEntity existing = requireEstab(command.getEstabId());
        existing.setEstabName(command.getEstabName().trim());
        existing.setEstabShortName(trimToNull(command.getEstabShortName()));
        existing.setEstabType(defaultIfNull(command.getEstabType(), existing.getEstabType()));
        existing.setStatus(defaultIfNull(command.getStatus(), existing.getStatus()));
        existing.setIndustryCode(trimToNull(command.getIndustryCode()));
        existing.setSizeRange(trimToNull(command.getSizeRange()));
        existing.setOwnerUserId(command.getOwnerUserId());
        existing.setContactName(trimToNull(command.getContactName()));
        existing.setContactPhone(trimToNull(command.getContactPhone()));
        existing.setContactEmail(trimToNull(command.getContactEmail()));
        existing.setWebsiteUrl(trimToNull(command.getWebsiteUrl()));
        existing.setLogoUrl(trimToNull(command.getLogoUrl()));
        existing.setRemark(trimToNull(command.getRemark()));

        organizationRepository.updateEstab(existing);
        return toEstabDto(requireEstab(existing.getId()));
    }

    /**
     * 删除企业
     *
     * @param estabId 企业ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteEstab(Long estabId) {
        EstabEntity estab = requireEstab(estabId);
        if (organizationRepository.countEstabUsers(estab.getId()) > 0) {
            throw new BizException(SystemErrorCode.ESTAB_HAS_USERS);
        }
        if (organizationRepository.countEstabTeams(estab.getId()) > 0) {
            throw new BizException(SystemErrorCode.ESTAB_HAS_TEAMS);
        }
        organizationRepository.deleteEstab(estab.getId());
    }

    /**
     * 查询企业地址列表
     *
     * @param estabId  企业ID
     * @param addrType 地址类型
     * @return 地址列表
     */
    public PageResponse<EstabAddressDTO> listEstabAddresses(Long estabId, Integer addrType, int currentPage, int pageSize) {
        requireEstab(estabId);
        PageResponse<EstabAddressEntity> entities = organizationRepository.listEstabAddresses(
                estabId,
                addrType,
                PageUtils.normalizeCurrentPage(currentPage),
                PageUtils.normalizePageSize(pageSize, PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE)
        );
        List<EstabAddressDTO> result = new ArrayList<>();
        for (EstabAddressEntity entity : entities.getData()) {
            result.add(toEstabAddressDto(entity));
        }
        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 新增企业地址
     *
     * @param command 创建命令
     * @return 地址详情
     */
    @Transactional(rollbackFor = Exception.class)
    public EstabAddressDTO createEstabAddress(CreateEstabAddressCommand command) {
        if (command == null || command.getEstabId() == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        requireEstab(command.getEstabId());

        EstabAddressEntity address = new EstabAddressEntity();
        address.setEstabId(command.getEstabId());
        address.setAddrType(defaultIfNull(command.getAddrType(), 2));
        address.setCountryCode(trimToNull(command.getCountryCode()));
        address.setProvinceCode(trimToNull(command.getProvinceCode()));
        address.setCityCode(trimToNull(command.getCityCode()));
        address.setDistrictCode(trimToNull(command.getDistrictCode()));
        address.setProvinceName(trimToNull(command.getProvinceName()));
        address.setCityName(trimToNull(command.getCityName()));
        address.setDistrictName(trimToNull(command.getDistrictName()));
        address.setAddressLine1(trimToNull(command.getAddressLine1()));
        address.setAddressLine2(trimToNull(command.getAddressLine2()));
        address.setPostalCode(trimToNull(command.getPostalCode()));
        address.setLatitude(command.getLatitude());
        address.setLongitude(command.getLongitude());
        address.setIsDefault(defaultIfNull(command.getIsDefault(), 0));
        address.setRemark(trimToNull(command.getRemark()));

        EstabAddressEntity created = organizationRepository.insertEstabAddress(address);
        if (created.getIsDefault() != null && created.getIsDefault() == 1) {
            organizationRepository.clearDefaultAddress(created.getEstabId(), created.getId());
        }
        return toEstabAddressDto(requireEstabAddress(created.getId()));
    }

    /**
     * 更新企业地址
     *
     * @param command 更新命令
     * @return 地址详情
     */
    @Transactional(rollbackFor = Exception.class)
    public EstabAddressDTO updateEstabAddress(UpdateEstabAddressCommand command) {
        if (command == null || command.getAddressId() == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        EstabAddressEntity existing = requireEstabAddress(command.getAddressId());
        existing.setAddrType(defaultIfNull(command.getAddrType(), existing.getAddrType()));
        existing.setCountryCode(trimToNull(command.getCountryCode()));
        existing.setProvinceCode(trimToNull(command.getProvinceCode()));
        existing.setCityCode(trimToNull(command.getCityCode()));
        existing.setDistrictCode(trimToNull(command.getDistrictCode()));
        existing.setProvinceName(trimToNull(command.getProvinceName()));
        existing.setCityName(trimToNull(command.getCityName()));
        existing.setDistrictName(trimToNull(command.getDistrictName()));
        existing.setAddressLine1(trimToNull(command.getAddressLine1()));
        existing.setAddressLine2(trimToNull(command.getAddressLine2()));
        existing.setPostalCode(trimToNull(command.getPostalCode()));
        existing.setLatitude(command.getLatitude());
        existing.setLongitude(command.getLongitude());
        existing.setIsDefault(defaultIfNull(command.getIsDefault(), existing.getIsDefault()));
        existing.setRemark(trimToNull(command.getRemark()));

        organizationRepository.updateEstabAddress(existing);
        if (existing.getIsDefault() != null && existing.getIsDefault() == 1) {
            organizationRepository.clearDefaultAddress(existing.getEstabId(), existing.getId());
        }
        return toEstabAddressDto(requireEstabAddress(existing.getId()));
    }

    /**
     * 删除企业地址
     *
     * @param addressId 地址ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteEstabAddress(Long addressId) {
        EstabAddressEntity address = requireEstabAddress(addressId);
        organizationRepository.deleteEstabAddress(address.getId());
    }

    /**
     * 查询企业认证策略
     *
     * @param estabId 企业ID
     * @return 认证策略
     */
    public EstabAuthPolicyDTO getEstabAuthPolicy(Long estabId) {
        requireEstab(estabId);
        EstabAuthPolicyEntity policy = organizationRepository.findEstabAuthPolicy(estabId);
        if (policy == null) {
            policy = buildDefaultPolicy(estabId);
        }
        return toEstabAuthPolicyDto(policy);
    }

    /**
     * 更新企业认证策略
     *
     * @param command 更新命令
     * @return 认证策略
     */
    @Transactional(rollbackFor = Exception.class)
    public EstabAuthPolicyDTO updateEstabAuthPolicy(UpdateEstabAuthPolicyCommand command) {
        if (command == null || command.getEstabId() == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        requireEstab(command.getEstabId());

        EstabAuthPolicyEntity policy = organizationRepository.findEstabAuthPolicy(command.getEstabId());
        if (policy == null) {
            policy = buildDefaultPolicy(command.getEstabId());
        }

        policy.setPasswordLoginEnabled(defaultIfNull(command.getPasswordLoginEnabled(), policy.getPasswordLoginEnabled()));
        policy.setSmsLoginEnabled(defaultIfNull(command.getSmsLoginEnabled(), policy.getSmsLoginEnabled()));
        policy.setEmailLoginEnabled(defaultIfNull(command.getEmailLoginEnabled(), policy.getEmailLoginEnabled()));
        policy.setWechatLoginEnabled(defaultIfNull(command.getWechatLoginEnabled(), policy.getWechatLoginEnabled()));
        policy.setMfaRequired(defaultIfNull(command.getMfaRequired(), policy.getMfaRequired()));
        policy.setMfaMethods(trimToNull(command.getMfaMethods()));
        policy.setPasswordMinLen(defaultIfNull(command.getPasswordMinLen(), policy.getPasswordMinLen()));
        policy.setPasswordStrength(defaultIfNull(command.getPasswordStrength(), policy.getPasswordStrength()));
        policy.setPasswordExpireDays(defaultIfNull(command.getPasswordExpireDays(), policy.getPasswordExpireDays()));
        policy.setLoginFailThreshold(defaultIfNull(command.getLoginFailThreshold(), policy.getLoginFailThreshold()));
        policy.setLockMinutes(defaultIfNull(command.getLockMinutes(), policy.getLockMinutes()));
        policy.setSessionTimeoutMinutes(defaultIfNull(command.getSessionTimeoutMinutes(), policy.getSessionTimeoutMinutes()));
        policy.setRemark(trimToNull(command.getRemark()));

        EstabAuthPolicyEntity saved = organizationRepository.saveEstabAuthPolicy(policy);
        return toEstabAuthPolicyDto(saved);
    }

    /**
     * 查询企业成员列表
     *
     * @param estabId 企业ID
     * @param status  状态
     * @return 成员列表
     */
    public PageResponse<EstabUserDTO> listEstabUsers(Long estabId, Integer status, int currentPage, int pageSize) {
        requireEstab(estabId);
        PageResponse<EstabUserEntity> entities = organizationRepository.listEstabUsers(
                estabId,
                status,
                PageUtils.normalizeCurrentPage(currentPage),
                PageUtils.normalizePageSize(pageSize, PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE)
        );
        List<EstabUserDTO> result = new ArrayList<>();
        for (EstabUserEntity entity : entities.getData()) {
            result.add(toEstabUserDto(entity));
        }
        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 新增企业成员关系
     *
     * @param command 创建命令
     * @return 成员关系
     */
    @Transactional(rollbackFor = Exception.class)
    public EstabUserDTO createEstabUser(CreateEstabUserCommand command) {
        if (command == null || command.getEstabId() == null || command.getUserId() == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        requireEstab(command.getEstabId());
        if (organizationRepository.countEstabUserRelation(command.getEstabId(), command.getUserId(), null) > 0) {
            throw new BizException(SystemErrorCode.ESTAB_USER_DUPLICATED);
        }

        EstabUserEntity estabUser = new EstabUserEntity();
        estabUser.setEstabId(command.getEstabId());
        estabUser.setUserId(command.getUserId());
        estabUser.setMemberType(defaultIfNull(command.getMemberType(), 0));
        estabUser.setIsAdmin(defaultIfNull(command.getIsAdmin(), 0));
        estabUser.setStatus(defaultIfNull(command.getStatus(), 1));
        estabUser.setJoinTime(defaultIfNull(command.getJoinTime(), LocalDateTime.now()));
        estabUser.setLeaveTime(command.getLeaveTime());
        estabUser.setPositionTitle(trimToNull(command.getPositionTitle()));

        return toEstabUserDto(organizationRepository.insertEstabUser(estabUser));
    }

    /**
     * 更新企业成员关系
     *
     * @param command 更新命令
     * @return 成员关系
     */
    @Transactional(rollbackFor = Exception.class)
    public EstabUserDTO updateEstabUser(UpdateEstabUserCommand command) {
        if (command == null || command.getEstabUserId() == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        EstabUserEntity existing = requireEstabUser(command.getEstabUserId());
        existing.setMemberType(defaultIfNull(command.getMemberType(), existing.getMemberType()));
        existing.setIsAdmin(defaultIfNull(command.getIsAdmin(), existing.getIsAdmin()));
        existing.setStatus(defaultIfNull(command.getStatus(), existing.getStatus()));
        existing.setJoinTime(defaultIfNull(command.getJoinTime(), existing.getJoinTime()));
        existing.setLeaveTime(command.getLeaveTime());
        existing.setPositionTitle(trimToNull(command.getPositionTitle()));
        organizationRepository.updateEstabUser(existing);
        return toEstabUserDto(requireEstabUser(existing.getId()));
    }

    /**
     * 删除企业成员关系
     *
     * @param estabUserId 企业成员关系ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteEstabUser(Long estabUserId) {
        EstabUserEntity estabUser = requireEstabUser(estabUserId);
        organizationRepository.deleteEstabUser(estabUser.getId());
    }

    /**
     * 查询团队列表
     *
     * @param command 查询命令
     * @return 团队列表
     */
    public PageResponse<TeamDTO> listTeams(QueryTeamListCommand command) {
        if (command == null || command.getEstabId() == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        requireEstab(command.getEstabId());
        int currentPage = PageUtils.normalizeCurrentPage(command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);
        PageResponse<TeamEntity> entities = organizationRepository.listTeams(
                command.getEstabId(),
                command.getParentId(),
                command.getStatus(),
                command.getKeyword(),
                currentPage,
                pageSize
        );
        List<TeamDTO> result = new ArrayList<>();
        for (TeamEntity entity : entities.getData()) {
            result.add(toTeamDto(entity));
        }
        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询团队详情
     *
     * @param teamId 团队ID
     * @return 团队详情
     */
    public TeamDTO getTeam(Long teamId) {
        return toTeamDto(requireTeam(teamId));
    }

    /**
     * 创建团队
     *
     * @param command 创建命令
     * @return 团队详情
     */
    @Transactional(rollbackFor = Exception.class)
    public TeamDTO createTeam(CreateTeamCommand command) {
        if (command == null || command.getEstabId() == null || isBlank(command.getTeamCode()) || isBlank(command.getTeamName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        requireEstab(command.getEstabId());

        String teamCode = command.getTeamCode().trim();
        if (organizationRepository.countTeamCode(command.getEstabId(), teamCode, null) > 0) {
            throw new BizException(SystemErrorCode.TEAM_CODE_DUPLICATED);
        }

        Long parentId = defaultIfNull(command.getParentId(), 0L);
        if (parentId > 0) {
            TeamEntity parent = requireTeam(parentId);
            if (!parent.getEstabId().equals(command.getEstabId())) {
                throw new BizException(SystemErrorCode.INVALID_PARAM);
            }
        }

        TeamEntity team = new TeamEntity();
        team.setEstabId(command.getEstabId());
        team.setTeamCode(teamCode);
        team.setTeamName(command.getTeamName().trim());
        team.setParentId(parentId);
        team.setLeaderUserId(command.getLeaderUserId());
        team.setStatus(defaultIfNull(command.getStatus(), 1));
        team.setSort(defaultIfNull(command.getSort(), 0));
        team.setRemark(trimToNull(command.getRemark()));

        return toTeamDto(organizationRepository.insertTeam(team));
    }

    /**
     * 更新团队
     *
     * @param command 更新命令
     * @return 团队详情
     */
    @Transactional(rollbackFor = Exception.class)
    public TeamDTO updateTeam(UpdateTeamCommand command) {
        if (command == null || command.getTeamId() == null || isBlank(command.getTeamName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        TeamEntity existing = requireTeam(command.getTeamId());
        Long parentId = defaultIfNull(command.getParentId(), existing.getParentId());
        if (parentId != null && parentId > 0) {
            if (parentId.equals(existing.getId())) {
                throw new BizException(SystemErrorCode.TEAM_PARENT_INVALID);
            }
            TeamEntity parent = requireTeam(parentId);
            if (!parent.getEstabId().equals(existing.getEstabId())) {
                throw new BizException(SystemErrorCode.TEAM_PARENT_INVALID);
            }
        }

        existing.setTeamName(command.getTeamName().trim());
        existing.setParentId(parentId);
        existing.setLeaderUserId(command.getLeaderUserId());
        existing.setStatus(defaultIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(defaultIfNull(command.getSort(), existing.getSort()));
        existing.setRemark(trimToNull(command.getRemark()));

        organizationRepository.updateTeam(existing);
        return toTeamDto(requireTeam(existing.getId()));
    }

    /**
     * 删除团队
     *
     * @param teamId 团队ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTeam(Long teamId) {
        TeamEntity team = requireTeam(teamId);
        if (organizationRepository.countChildTeams(team.getId()) > 0) {
            throw new BizException(SystemErrorCode.TEAM_HAS_CHILDREN);
        }
        if (organizationRepository.countTeamUsers(team.getId()) > 0) {
            throw new BizException(SystemErrorCode.TEAM_HAS_USERS);
        }
        organizationRepository.deleteTeam(team.getId());
    }

    /**
     * 查询团队成员列表
     *
     * @param teamId 团队ID
     * @param status 状态
     * @return 团队成员列表
     */
    public PageResponse<TeamUserDTO> listTeamUsers(Long teamId, Integer status, int currentPage, int pageSize) {
        requireTeam(teamId);
        PageResponse<TeamUserEntity> entities = organizationRepository.listTeamUsers(
                teamId,
                status,
                PageUtils.normalizeCurrentPage(currentPage),
                PageUtils.normalizePageSize(pageSize, PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE)
        );
        List<TeamUserDTO> result = new ArrayList<>();
        for (TeamUserEntity entity : entities.getData()) {
            result.add(toTeamUserDto(entity));
        }
        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 创建团队成员关系
     *
     * @param command 创建命令
     * @return 团队成员关系
     */
    @Transactional(rollbackFor = Exception.class)
    public TeamUserDTO createTeamUser(CreateTeamUserCommand command) {
        if (command == null || command.getTeamId() == null || command.getUserId() == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        requireTeam(command.getTeamId());
        if (organizationRepository.countTeamUserRelation(command.getTeamId(), command.getUserId(), null) > 0) {
            throw new BizException(SystemErrorCode.TEAM_USER_DUPLICATED);
        }

        TeamUserEntity teamUser = new TeamUserEntity();
        teamUser.setTeamId(command.getTeamId());
        teamUser.setUserId(command.getUserId());
        teamUser.setRoleInTeam(defaultIfNull(command.getRoleInTeam(), 0));
        teamUser.setStatus(defaultIfNull(command.getStatus(), 1));
        teamUser.setJoinTime(defaultIfNull(command.getJoinTime(), LocalDateTime.now()));

        return toTeamUserDto(organizationRepository.insertTeamUser(teamUser));
    }

    /**
     * 更新团队成员关系
     *
     * @param command 更新命令
     * @return 团队成员关系
     */
    @Transactional(rollbackFor = Exception.class)
    public TeamUserDTO updateTeamUser(UpdateTeamUserCommand command) {
        if (command == null || command.getTeamUserId() == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        TeamUserEntity existing = requireTeamUser(command.getTeamUserId());
        existing.setRoleInTeam(defaultIfNull(command.getRoleInTeam(), existing.getRoleInTeam()));
        existing.setStatus(defaultIfNull(command.getStatus(), existing.getStatus()));
        existing.setJoinTime(defaultIfNull(command.getJoinTime(), existing.getJoinTime()));

        organizationRepository.updateTeamUser(existing);
        return toTeamUserDto(requireTeamUser(existing.getId()));
    }

    /**
     * 删除团队成员关系
     *
     * @param teamUserId 团队成员关系ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTeamUser(Long teamUserId) {
        TeamUserEntity teamUser = requireTeamUser(teamUserId);
        organizationRepository.deleteTeamUser(teamUser.getId());
    }

    private EstabEntity requireEstab(Long estabId) {
        if (estabId == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        EstabEntity estab = organizationRepository.findEstabById(estabId);
        if (estab == null || (estab.getDeleted() != null && estab.getDeleted() == 1)) {
            throw new BizException(SystemErrorCode.ESTAB_NOT_FOUND);
        }
        return estab;
    }

    private EstabAddressEntity requireEstabAddress(Long addressId) {
        if (addressId == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        EstabAddressEntity address = organizationRepository.findEstabAddressById(addressId);
        if (address == null || (address.getDeleted() != null && address.getDeleted() == 1)) {
            throw new BizException(SystemErrorCode.ESTAB_ADDRESS_NOT_FOUND);
        }
        return address;
    }

    private EstabUserEntity requireEstabUser(Long estabUserId) {
        if (estabUserId == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        EstabUserEntity estabUser = organizationRepository.findEstabUserById(estabUserId);
        if (estabUser == null || (estabUser.getDeleted() != null && estabUser.getDeleted() == 1)) {
            throw new BizException(SystemErrorCode.ESTAB_USER_NOT_FOUND);
        }
        return estabUser;
    }

    private TeamEntity requireTeam(Long teamId) {
        if (teamId == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        TeamEntity team = organizationRepository.findTeamById(teamId);
        if (team == null || (team.getDeleted() != null && team.getDeleted() == 1)) {
            throw new BizException(SystemErrorCode.TEAM_NOT_FOUND);
        }
        return team;
    }

    private TeamUserEntity requireTeamUser(Long teamUserId) {
        if (teamUserId == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        TeamUserEntity teamUser = organizationRepository.findTeamUserById(teamUserId);
        if (teamUser == null || (teamUser.getDeleted() != null && teamUser.getDeleted() == 1)) {
            throw new BizException(SystemErrorCode.TEAM_USER_NOT_FOUND);
        }
        return teamUser;
    }

    private EstabAuthPolicyEntity buildDefaultPolicy(Long estabId) {
        EstabAuthPolicyEntity policy = new EstabAuthPolicyEntity();
        policy.setEstabId(estabId);
        policy.setPasswordLoginEnabled(1);
        policy.setSmsLoginEnabled(1);
        policy.setEmailLoginEnabled(1);
        policy.setWechatLoginEnabled(0);
        policy.setMfaRequired(0);
        policy.setPasswordMinLen(8);
        policy.setPasswordStrength(1);
        policy.setPasswordExpireDays(0);
        policy.setLoginFailThreshold(5);
        policy.setLockMinutes(30);
        policy.setSessionTimeoutMinutes(120);
        return policy;
    }

    private EstabDTO toEstabDto(EstabEntity entity) {
        EstabDTO dto = new EstabDTO();
        dto.setId(entity.getId());
        dto.setEstabCode(entity.getEstabCode());
        dto.setEstabName(entity.getEstabName());
        dto.setEstabShortName(entity.getEstabShortName());
        dto.setEstabType(entity.getEstabType());
        dto.setStatus(entity.getStatus());
        dto.setIndustryCode(entity.getIndustryCode());
        dto.setSizeRange(entity.getSizeRange());
        dto.setOwnerUserId(entity.getOwnerUserId());
        dto.setContactName(entity.getContactName());
        dto.setContactPhone(entity.getContactPhone());
        dto.setContactEmail(entity.getContactEmail());
        dto.setWebsiteUrl(entity.getWebsiteUrl());
        dto.setLogoUrl(entity.getLogoUrl());
        dto.setRemark(entity.getRemark());
        return dto;
    }

    private EstabAddressDTO toEstabAddressDto(EstabAddressEntity entity) {
        EstabAddressDTO dto = new EstabAddressDTO();
        dto.setId(entity.getId());
        dto.setEstabId(entity.getEstabId());
        dto.setAddrType(entity.getAddrType());
        dto.setCountryCode(entity.getCountryCode());
        dto.setProvinceCode(entity.getProvinceCode());
        dto.setCityCode(entity.getCityCode());
        dto.setDistrictCode(entity.getDistrictCode());
        dto.setProvinceName(entity.getProvinceName());
        dto.setCityName(entity.getCityName());
        dto.setDistrictName(entity.getDistrictName());
        dto.setAddressLine1(entity.getAddressLine1());
        dto.setAddressLine2(entity.getAddressLine2());
        dto.setPostalCode(entity.getPostalCode());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setIsDefault(entity.getIsDefault());
        dto.setRemark(entity.getRemark());
        return dto;
    }

    private EstabAuthPolicyDTO toEstabAuthPolicyDto(EstabAuthPolicyEntity entity) {
        EstabAuthPolicyDTO dto = new EstabAuthPolicyDTO();
        dto.setId(entity.getId());
        dto.setEstabId(entity.getEstabId());
        dto.setPasswordLoginEnabled(entity.getPasswordLoginEnabled());
        dto.setSmsLoginEnabled(entity.getSmsLoginEnabled());
        dto.setEmailLoginEnabled(entity.getEmailLoginEnabled());
        dto.setWechatLoginEnabled(entity.getWechatLoginEnabled());
        dto.setMfaRequired(entity.getMfaRequired());
        dto.setMfaMethods(entity.getMfaMethods());
        dto.setPasswordMinLen(entity.getPasswordMinLen());
        dto.setPasswordStrength(entity.getPasswordStrength());
        dto.setPasswordExpireDays(entity.getPasswordExpireDays());
        dto.setLoginFailThreshold(entity.getLoginFailThreshold());
        dto.setLockMinutes(entity.getLockMinutes());
        dto.setSessionTimeoutMinutes(entity.getSessionTimeoutMinutes());
        dto.setRemark(entity.getRemark());
        return dto;
    }

    private EstabUserDTO toEstabUserDto(EstabUserEntity entity) {
        EstabUserDTO dto = new EstabUserDTO();
        dto.setId(entity.getId());
        dto.setEstabId(entity.getEstabId());
        dto.setUserId(entity.getUserId());
        dto.setMemberType(entity.getMemberType());
        dto.setIsAdmin(entity.getIsAdmin());
        dto.setStatus(entity.getStatus());
        dto.setJoinTime(entity.getJoinTime());
        dto.setLeaveTime(entity.getLeaveTime());
        dto.setPositionTitle(entity.getPositionTitle());
        return dto;
    }

    private TeamDTO toTeamDto(TeamEntity entity) {
        TeamDTO dto = new TeamDTO();
        dto.setId(entity.getId());
        dto.setEstabId(entity.getEstabId());
        dto.setTeamCode(entity.getTeamCode());
        dto.setTeamName(entity.getTeamName());
        dto.setParentId(entity.getParentId());
        dto.setLeaderUserId(entity.getLeaderUserId());
        dto.setStatus(entity.getStatus());
        dto.setSort(entity.getSort());
        dto.setRemark(entity.getRemark());
        return dto;
    }

    private TeamUserDTO toTeamUserDto(TeamUserEntity entity) {
        TeamUserDTO dto = new TeamUserDTO();
        dto.setId(entity.getId());
        dto.setTeamId(entity.getTeamId());
        dto.setUserId(entity.getUserId());
        dto.setRoleInTeam(entity.getRoleInTeam());
        dto.setStatus(entity.getStatus());
        dto.setJoinTime(entity.getJoinTime());
        return dto;
    }
}
