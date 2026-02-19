package cn.refinex.system.application.service;

import cn.refinex.api.user.model.dto.UserManageDTO;
import cn.refinex.api.user.model.dto.UserManageListQuery;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.PageResponse;
import cn.refinex.base.utils.PageUtils;
import cn.refinex.base.utils.UniqueCodeUtils;
import cn.refinex.system.application.assembler.SystemDomainAssembler;
import cn.refinex.system.application.command.*;
import cn.refinex.system.application.dto.*;
import cn.refinex.system.domain.error.SystemErrorCode;
import cn.refinex.system.domain.model.entity.*;
import cn.refinex.system.domain.repository.OrganizationRepository;
import cn.refinex.system.infrastructure.client.user.UserManageRemoteGateway;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static cn.refinex.base.utils.ValueUtils.*;

/**
 * 企业与组织结构应用服务
 *
 * @author refinex
 */
@Service
@RequiredArgsConstructor
public class OrganizationApplicationService {

    private static final String TEAM_CODE_PREFIX = "TEAM_";
    private static final int TEAM_CODE_RANDOM_LENGTH = 10;
    private static final int TEAM_CODE_GENERATE_MAX_RETRY = 20;

    private final OrganizationRepository organizationRepository;
    private final SystemDomainAssembler systemDomainAssembler;
    private final UserManageRemoteGateway userManageRemoteGateway;

    /**
     * 查询企业列表
     *
     * @param command 查询命令
     * @return 企业列表
     */
    public PageResponse<EstabDTO> listEstabs(QueryEstabListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(), PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);

        PageResponse<EstabEntity> entities = organizationRepository.listEstabs(
                command == null ? null : command.getStatus(),
                command == null ? null : command.getEstabType(),
                command == null ? null : command.getKeyword(),
                currentPage,
                pageSize
        );

        List<EstabDTO> result = new ArrayList<>();
        for (EstabEntity entity : entities.getData()) {
            result.add(systemDomainAssembler.toEstabDto(entity));
        }

        enrichEstabs(result);

        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询企业详情
     *
     * @param estabId 企业ID
     * @return 企业详情
     */
    public EstabDTO getEstab(Long estabId) {
        EstabDTO estabDto = systemDomainAssembler.toEstabDto(requireEstab(estabId));
        enrichEstabs(List.of(estabDto));
        return estabDto;
    }

    /**
     * 补充企业负责人展示信息
     *
     * @param estabDtos 企业列表
     */
    private void enrichEstabs(List<EstabDTO> estabDtos) {
        if (CollectionUtils.isEmpty(estabDtos)) {
            return;
        }

        List<Long> ownerUserIds = estabDtos.stream()
                .map(EstabDTO::getOwnerUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, UserManageDTO> userMap = loadUserMapByIds(ownerUserIds);

        for (EstabDTO estabDto : estabDtos) {
            fillEstabOwnerDisplayFields(estabDto, userMap.get(estabDto.getOwnerUserId()));
        }
    }

    /**
     * 将负责人信息写入企业DTO
     *
     * @param estabDto 企业DTO
     * @param user     用户信息
     */
    private void fillEstabOwnerDisplayFields(EstabDTO estabDto, UserManageDTO user) {
        if (estabDto == null || user == null) {
            return;
        }

        estabDto.setOwnerUsername(user.getUsername());
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
        return systemDomainAssembler.toEstabDto(created);
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
        return systemDomainAssembler.toEstabDto(requireEstab(existing.getId()));
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
            result.add(systemDomainAssembler.toEstabAddressDto(entity));
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
        return systemDomainAssembler.toEstabAddressDto(requireEstabAddress(created.getId()));
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
        return systemDomainAssembler.toEstabAddressDto(requireEstabAddress(existing.getId()));
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
        return systemDomainAssembler.toEstabAuthPolicyDto(policy);
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
        return systemDomainAssembler.toEstabAuthPolicyDto(saved);
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
            result.add(systemDomainAssembler.toEstabUserDto(entity));
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

        return systemDomainAssembler.toEstabUserDto(organizationRepository.insertEstabUser(estabUser));
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
        return systemDomainAssembler.toEstabUserDto(requireEstabUser(existing.getId()));
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
        int pageSize = PageUtils.normalizePageSize(command.getPageSize(), PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);

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
            result.add(systemDomainAssembler.toTeamDto(entity));
        }
        enrichTeams(result);
        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询团队详情
     *
     * @param teamId 团队ID
     * @return 团队详情
     */
    public TeamDTO getTeam(Long teamId) {
        TeamDTO dto = systemDomainAssembler.toTeamDto(requireTeam(teamId));
        enrichTeam(dto);
        return dto;
    }

    /**
     * 创建团队
     *
     * @param command 创建命令
     * @return 团队详情
     */
    @Transactional(rollbackFor = Exception.class)
    public TeamDTO createTeam(CreateTeamCommand command) {
        if (command == null || command.getEstabId() == null || isBlank(command.getTeamName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        requireEstab(command.getEstabId());

        Long parentId = defaultIfNull(command.getParentId(), 0L);
        if (parentId > 0) {
            TeamEntity parent = requireTeam(parentId);
            if (!parent.getEstabId().equals(command.getEstabId())) {
                throw new BizException(SystemErrorCode.INVALID_PARAM);
            }
        }

        String teamCode = generateUniqueTeamCode(command.getEstabId());
        TeamEntity team = new TeamEntity();
        team.setEstabId(command.getEstabId());
        team.setTeamCode(teamCode);
        team.setTeamName(command.getTeamName().trim());
        team.setParentId(parentId);
        team.setLeaderUserId(command.getLeaderUserId());
        team.setStatus(defaultIfNull(command.getStatus(), 1));
        team.setSort(defaultIfNull(command.getSort(), 0));
        team.setRemark(trimToNull(command.getRemark()));

        TeamDTO dto = systemDomainAssembler.toTeamDto(organizationRepository.insertTeam(team));
        enrichTeam(dto);
        return dto;
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
        TeamDTO dto = systemDomainAssembler.toTeamDto(requireTeam(existing.getId()));
        enrichTeam(dto);
        return dto;
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
     * 生成团队唯一编码（大写）
     *
     * @param estabId 企业ID
     * @return 唯一团队编码
     */
    private String generateUniqueTeamCode(Long estabId) {
        for (int i = 0; i < TEAM_CODE_GENERATE_MAX_RETRY; i++) {
            String code = UniqueCodeUtils.randomUpperCode(TEAM_CODE_PREFIX, TEAM_CODE_RANDOM_LENGTH);
            if (organizationRepository.countTeamCode(estabId, code, null) == 0) {
                return code;
            }
        }
        throw new BizException(SystemErrorCode.TEAM_CODE_DUPLICATED);
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
            result.add(systemDomainAssembler.toTeamUserDto(entity));
        }
        enrichTeamUsers(result);
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

        TeamUserDTO dto = systemDomainAssembler.toTeamUserDto(organizationRepository.insertTeamUser(teamUser));
        enrichTeamUser(dto);
        return dto;
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
        TeamUserDTO dto = systemDomainAssembler.toTeamUserDto(requireTeamUser(existing.getId()));
        enrichTeamUser(dto);
        return dto;
    }

    /**
     * 查询团队成员候选用户（用于前端联想）
     *
     * @param teamId  团队ID
     * @param keyword 用户名关键字
     * @param limit   返回条数
     * @return 候选用户列表
     */
    public List<TeamUserCandidateDTO> listTeamUserCandidates(Long teamId, String keyword, Integer limit) {
        TeamEntity team = requireTeam(teamId);
        String safeKeyword = trimToNull(keyword);
        if (safeKeyword == null) {
            return Collections.emptyList();
        }

        int safeLimit = limit == null ? 10 : Math.max(1, Math.min(limit, 50));
        List<Long> existingUserIds = organizationRepository.listTeamUserIds(teamId);
        UserManageListQuery query = new UserManageListQuery();
        query.setPrimaryEstabId(team.getEstabId());
        query.setKeyword(safeKeyword);
        query.setStatus(1);
        query.setCurrentPage(1);
        query.setPageSize(safeLimit * 3);

        PageResponse<UserManageDTO> users = userManageRemoteGateway.listUsers(query);
        List<TeamUserCandidateDTO> result = new ArrayList<>();
        List<UserManageDTO> rows = users.getData() == null ? Collections.emptyList() : users.getData();
        for (UserManageDTO user : rows) {
            if (user == null || user.getUserId() == null) {
                continue;
            }
            if (existingUserIds.contains(user.getUserId())) {
                continue;
            }
            TeamUserCandidateDTO dto = new TeamUserCandidateDTO();
            dto.setUserId(user.getUserId());
            dto.setUsername(user.getUsername());
            dto.setUserCode(user.getUserCode());
            dto.setDisplayName(user.getDisplayName());
            result.add(dto);
            if (result.size() >= safeLimit) {
                break;
            }
        }
        return result;
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

    /**
     * 批量补充团队负责人展示信息
     *
     * @param teams 团队列表
     */
    private void enrichTeams(List<TeamDTO> teams) {
        if (teams == null || teams.isEmpty()) {
            return;
        }

        List<Long> leaderUserIds = teams.stream()
                .map(TeamDTO::getLeaderUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, UserManageDTO> userMap = loadUserMapByIds(leaderUserIds);
        for (TeamDTO team : teams) {
            fillTeamLeaderDisplayFields(team, userMap.get(team.getLeaderUserId()));
        }
    }

    /**
     * 补充单个团队负责人展示信息
     *
     * @param team 团队
     */
    private void enrichTeam(TeamDTO team) {
        if (team == null || team.getLeaderUserId() == null) {
            fillTeamLeaderDisplayFields(team, null);
            return;
        }
        Map<Long, UserManageDTO> userMap = loadUserMapByIds(List.of(team.getLeaderUserId()));
        fillTeamLeaderDisplayFields(team, userMap.get(team.getLeaderUserId()));
    }

    /**
     * 将负责人信息写入团队DTO
     *
     * @param team 团队DTO
     * @param user 用户信息
     */
    private void fillTeamLeaderDisplayFields(TeamDTO team, UserManageDTO user) {
        if (team == null) {
            return;
        }
        if (user == null) {
            team.setLeaderUsername(null);
            team.setLeaderUserCode(null);
            team.setLeaderDisplayName(null);
            return;
        }
        team.setLeaderUsername(user.getUsername());
        team.setLeaderUserCode(user.getUserCode());
        team.setLeaderDisplayName(user.getDisplayName());
    }

    /**
     * 批量补充团队成员用户信息
     *
     * @param teamUsers 团队成员列表
     */
    private void enrichTeamUsers(List<TeamUserDTO> teamUsers) {
        if (teamUsers == null || teamUsers.isEmpty()) {
            return;
        }
        List<Long> userIds = teamUsers.stream()
                .map(TeamUserDTO::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, UserManageDTO> userMap = loadUserMapByIds(userIds);
        for (TeamUserDTO teamUser : teamUsers) {
            fillTeamUserDisplayFields(teamUser, userMap.get(teamUser.getUserId()));
        }
    }

    /**
     * 补充单个团队成员用户信息
     *
     * @param teamUser 团队成员
     */
    private void enrichTeamUser(TeamUserDTO teamUser) {
        if (teamUser == null || teamUser.getUserId() == null) {
            return;
        }
        Map<Long, UserManageDTO> userMap = loadUserMapByIds(List.of(teamUser.getUserId()));
        fillTeamUserDisplayFields(teamUser, userMap.get(teamUser.getUserId()));
    }

    /**
     * 根据用户ID列表加载用户档案
     *
     * @param userIds 用户ID列表
     * @return 用户信息映射
     */
    private Map<Long, UserManageDTO> loadUserMapByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        UserManageListQuery query = new UserManageListQuery();
        query.setUserIds(userIds);
        query.setCurrentPage(1);
        query.setPageSize(userIds.size());
        PageResponse<UserManageDTO> response = userManageRemoteGateway.listUsers(query);

        Map<Long, UserManageDTO> userMap = new HashMap<>();
        List<UserManageDTO> rows = response.getData() == null ? Collections.emptyList() : response.getData();
        for (UserManageDTO user : rows) {
            if (user != null && user.getUserId() != null) {
                userMap.put(user.getUserId(), user);
            }
        }
        return userMap;
    }

    /**
     * 将用户信息写入团队成员DTO
     *
     * @param teamUser 团队成员DTO
     * @param user     用户信息
     */
    private void fillTeamUserDisplayFields(TeamUserDTO teamUser, UserManageDTO user) {
        if (teamUser == null || user == null) {
            return;
        }
        teamUser.setUsername(user.getUsername());
        teamUser.setUserCode(user.getUserCode());
        teamUser.setDisplayName(user.getDisplayName());
    }

    /**
     * 获取企业
     *
     * @param estabId 企业ID
     * @return 企业
     */
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

    /**
     * 获取企业地址
     *
     * @param addressId 地址ID
     * @return 地址
     */
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

    /**
     * 获取企业成员关系
     *
     * @param estabUserId 企业成员关系ID
     * @return 成员关系
     */
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

    /**
     * 获取团队
     *
     * @param teamId 团队ID
     * @return 团队
     */
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

    /**
     * 获取团队成员关系
     *
     * @param teamUserId 团队成员关系ID
     * @return 团队成员关系
     */
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

    /**
     * 构建默认策略
     *
     * @param estabId 企业ID
     * @return 默认策略
     */
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
}
