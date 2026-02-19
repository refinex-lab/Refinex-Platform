package cn.refinex.system.interfaces.controller;

import cn.refinex.base.response.MultiResponse;
import cn.refinex.base.response.SingleResponse;
import cn.refinex.system.application.command.*;
import cn.refinex.system.application.dto.*;
import cn.refinex.system.application.service.OrganizationApplicationService;
import cn.refinex.system.interfaces.assembler.OrganizationApiAssembler;
import cn.refinex.system.interfaces.dto.*;
import cn.refinex.system.interfaces.vo.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业与组织结构管理接口
 *
 * @author refinex
 */
@Validated
@RestController
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationApplicationService organizationApplicationService;
    private final OrganizationApiAssembler organizationApiAssembler;

    /**
     * 查询企业列表
     *
     * @param query 查询参数
     * @return 企业列表
     */
    @GetMapping("/estabs")
    public MultiResponse<EstabVO> listEstabs(@Valid EstabListQuery query) {
        QueryEstabListCommand command = organizationApiAssembler.toQueryEstabListCommand(query);
        List<EstabDTO> list = organizationApplicationService.listEstabs(command);
        return MultiResponse.of(organizationApiAssembler.toEstabVoList(list));
    }

    /**
     * 查询企业详情
     *
     * @param estabId 企业ID
     * @return 企业详情
     */
    @GetMapping("/estabs/{estabId}")
    public SingleResponse<EstabVO> getEstab(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId) {
        EstabDTO dto = organizationApplicationService.getEstab(estabId);
        return SingleResponse.of(organizationApiAssembler.toEstabVo(dto));
    }

    /**
     * 创建企业
     *
     * @param request 创建请求
     * @return 企业详情
     */
    @PostMapping("/estabs")
    public SingleResponse<EstabVO> createEstab(@Valid @RequestBody EstabCreateRequest request) {
        CreateEstabCommand command = organizationApiAssembler.toCreateEstabCommand(request);
        EstabDTO dto = organizationApplicationService.createEstab(command);
        return SingleResponse.of(organizationApiAssembler.toEstabVo(dto));
    }

    /**
     * 更新企业
     *
     * @param estabId 企业ID
     * @param request 更新请求
     * @return 企业详情
     */
    @PutMapping("/estabs/{estabId}")
    public SingleResponse<EstabVO> updateEstab(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId,
                                               @Valid @RequestBody EstabUpdateRequest request) {
        UpdateEstabCommand command = organizationApiAssembler.toUpdateEstabCommand(request);
        command.setEstabId(estabId);
        EstabDTO dto = organizationApplicationService.updateEstab(command);
        return SingleResponse.of(organizationApiAssembler.toEstabVo(dto));
    }

    /**
     * 删除企业
     *
     * @param estabId 企业ID
     * @return 操作结果
     */
    @DeleteMapping("/estabs/{estabId}")
    public SingleResponse<Void> deleteEstab(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId) {
        organizationApplicationService.deleteEstab(estabId);
        return SingleResponse.of(null);
    }

    /**
     * 查询企业地址列表
     *
     * @param estabId 企业ID
     * @param query   查询参数
     * @return 企业地址列表
     */
    @GetMapping("/estabs/{estabId}/addresses")
    public MultiResponse<EstabAddressVO> listEstabAddresses(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId,
                                                            @Valid EstabAddressListQuery query) {
        List<EstabAddressDTO> list = organizationApplicationService.listEstabAddresses(estabId, query.getAddrType());
        return MultiResponse.of(organizationApiAssembler.toEstabAddressVoList(list));
    }

    /**
     * 创建企业地址
     *
     * @param estabId 企业ID
     * @param request 创建请求
     * @return 地址详情
     */
    @PostMapping("/estabs/{estabId}/addresses")
    public SingleResponse<EstabAddressVO> createEstabAddress(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId,
                                                             @Valid @RequestBody EstabAddressCreateRequest request) {
        CreateEstabAddressCommand command = organizationApiAssembler.toCreateEstabAddressCommand(request);
        command.setEstabId(estabId);
        EstabAddressDTO dto = organizationApplicationService.createEstabAddress(command);
        return SingleResponse.of(organizationApiAssembler.toEstabAddressVo(dto));
    }

    /**
     * 更新企业地址
     *
     * @param addressId 地址ID
     * @param request   更新请求
     * @return 地址详情
     */
    @PutMapping("/estab-addresses/{addressId}")
    public SingleResponse<EstabAddressVO> updateEstabAddress(@PathVariable @Positive(message = "地址ID必须大于0") Long addressId,
                                                             @Valid @RequestBody EstabAddressUpdateRequest request) {
        UpdateEstabAddressCommand command = organizationApiAssembler.toUpdateEstabAddressCommand(request);
        command.setAddressId(addressId);
        EstabAddressDTO dto = organizationApplicationService.updateEstabAddress(command);
        return SingleResponse.of(organizationApiAssembler.toEstabAddressVo(dto));
    }

    /**
     * 删除企业地址
     *
     * @param addressId 地址ID
     * @return 操作结果
     */
    @DeleteMapping("/estab-addresses/{addressId}")
    public SingleResponse<Void> deleteEstabAddress(@PathVariable @Positive(message = "地址ID必须大于0") Long addressId) {
        organizationApplicationService.deleteEstabAddress(addressId);
        return SingleResponse.of(null);
    }

    /**
     * 查询企业认证策略
     *
     * @param estabId 企业ID
     * @return 认证策略
     */
    @GetMapping("/estabs/{estabId}/auth-policy")
    public SingleResponse<EstabAuthPolicyVO> getEstabAuthPolicy(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId) {
        EstabAuthPolicyDTO dto = organizationApplicationService.getEstabAuthPolicy(estabId);
        return SingleResponse.of(organizationApiAssembler.toEstabAuthPolicyVo(dto));
    }

    /**
     * 更新企业认证策略
     *
     * @param estabId 企业ID
     * @param request 更新请求
     * @return 认证策略
     */
    @PutMapping("/estabs/{estabId}/auth-policy")
    public SingleResponse<EstabAuthPolicyVO> updateEstabAuthPolicy(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId,
                                                                   @Valid @RequestBody EstabAuthPolicyUpdateRequest request) {
        UpdateEstabAuthPolicyCommand command = organizationApiAssembler.toUpdateEstabAuthPolicyCommand(request);
        command.setEstabId(estabId);
        EstabAuthPolicyDTO dto = organizationApplicationService.updateEstabAuthPolicy(command);
        return SingleResponse.of(organizationApiAssembler.toEstabAuthPolicyVo(dto));
    }

    /**
     * 查询企业成员列表
     *
     * @param estabId 企业ID
     * @param query   查询参数
     * @return 企业成员列表
     */
    @GetMapping("/estabs/{estabId}/users")
    public MultiResponse<EstabUserVO> listEstabUsers(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId,
                                                     @Valid EstabUserListQuery query) {
        List<EstabUserDTO> list = organizationApplicationService.listEstabUsers(estabId, query.getStatus());
        return MultiResponse.of(organizationApiAssembler.toEstabUserVoList(list));
    }

    /**
     * 创建企业成员关系
     *
     * @param estabId 企业ID
     * @param request 创建请求
     * @return 成员关系
     */
    @PostMapping("/estabs/{estabId}/users")
    public SingleResponse<EstabUserVO> createEstabUser(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId,
                                                       @Valid @RequestBody EstabUserCreateRequest request) {
        CreateEstabUserCommand command = organizationApiAssembler.toCreateEstabUserCommand(request);
        command.setEstabId(estabId);
        EstabUserDTO dto = organizationApplicationService.createEstabUser(command);
        return SingleResponse.of(organizationApiAssembler.toEstabUserVo(dto));
    }

    /**
     * 更新企业成员关系
     *
     * @param estabUserId 企业成员关系ID
     * @param request     更新请求
     * @return 成员关系
     */
    @PutMapping("/estab-users/{estabUserId}")
    public SingleResponse<EstabUserVO> updateEstabUser(@PathVariable @Positive(message = "企业成员关系ID必须大于0") Long estabUserId,
                                                       @Valid @RequestBody EstabUserUpdateRequest request) {
        UpdateEstabUserCommand command = organizationApiAssembler.toUpdateEstabUserCommand(request);
        command.setEstabUserId(estabUserId);
        EstabUserDTO dto = organizationApplicationService.updateEstabUser(command);
        return SingleResponse.of(organizationApiAssembler.toEstabUserVo(dto));
    }

    /**
     * 删除企业成员关系
     *
     * @param estabUserId 企业成员关系ID
     * @return 操作结果
     */
    @DeleteMapping("/estab-users/{estabUserId}")
    public SingleResponse<Void> deleteEstabUser(@PathVariable @Positive(message = "企业成员关系ID必须大于0") Long estabUserId) {
        organizationApplicationService.deleteEstabUser(estabUserId);
        return SingleResponse.of(null);
    }

    /**
     * 查询团队列表
     *
     * @param query 查询参数
     * @return 团队列表
     */
    @GetMapping("/teams")
    public MultiResponse<TeamVO> listTeams(@Valid TeamListQuery query) {
        QueryTeamListCommand command = organizationApiAssembler.toQueryTeamListCommand(query);
        List<TeamDTO> list = organizationApplicationService.listTeams(command);
        return MultiResponse.of(organizationApiAssembler.toTeamVoList(list));
    }

    /**
     * 查询团队详情
     *
     * @param teamId 团队ID
     * @return 团队详情
     */
    @GetMapping("/teams/{teamId}")
    public SingleResponse<TeamVO> getTeam(@PathVariable @Positive(message = "团队ID必须大于0") Long teamId) {
        TeamDTO dto = organizationApplicationService.getTeam(teamId);
        return SingleResponse.of(organizationApiAssembler.toTeamVo(dto));
    }

    /**
     * 创建团队
     *
     * @param request 创建请求
     * @return 团队详情
     */
    @PostMapping("/teams")
    public SingleResponse<TeamVO> createTeam(@Valid @RequestBody TeamCreateRequest request) {
        CreateTeamCommand command = organizationApiAssembler.toCreateTeamCommand(request);
        TeamDTO dto = organizationApplicationService.createTeam(command);
        return SingleResponse.of(organizationApiAssembler.toTeamVo(dto));
    }

    /**
     * 更新团队
     *
     * @param teamId  团队ID
     * @param request 更新请求
     * @return 团队详情
     */
    @PutMapping("/teams/{teamId}")
    public SingleResponse<TeamVO> updateTeam(@PathVariable @Positive(message = "团队ID必须大于0") Long teamId,
                                             @Valid @RequestBody TeamUpdateRequest request) {
        UpdateTeamCommand command = organizationApiAssembler.toUpdateTeamCommand(request);
        command.setTeamId(teamId);
        TeamDTO dto = organizationApplicationService.updateTeam(command);
        return SingleResponse.of(organizationApiAssembler.toTeamVo(dto));
    }

    /**
     * 删除团队
     *
     * @param teamId 团队ID
     * @return 操作结果
     */
    @DeleteMapping("/teams/{teamId}")
    public SingleResponse<Void> deleteTeam(@PathVariable @Positive(message = "团队ID必须大于0") Long teamId) {
        organizationApplicationService.deleteTeam(teamId);
        return SingleResponse.of(null);
    }

    /**
     * 查询团队成员列表
     *
     * @param teamId 团队ID
     * @param query  查询参数
     * @return 团队成员列表
     */
    @GetMapping("/teams/{teamId}/users")
    public MultiResponse<TeamUserVO> listTeamUsers(@PathVariable @Positive(message = "团队ID必须大于0") Long teamId,
                                                   @Valid TeamUserListQuery query) {
        List<TeamUserDTO> list = organizationApplicationService.listTeamUsers(teamId, query.getStatus());
        return MultiResponse.of(organizationApiAssembler.toTeamUserVoList(list));
    }

    /**
     * 创建团队成员关系
     *
     * @param teamId  团队ID
     * @param request 创建请求
     * @return 团队成员关系
     */
    @PostMapping("/teams/{teamId}/users")
    public SingleResponse<TeamUserVO> createTeamUser(@PathVariable @Positive(message = "团队ID必须大于0") Long teamId,
                                                     @Valid @RequestBody TeamUserCreateRequest request) {
        CreateTeamUserCommand command = organizationApiAssembler.toCreateTeamUserCommand(request);
        command.setTeamId(teamId);
        TeamUserDTO dto = organizationApplicationService.createTeamUser(command);
        return SingleResponse.of(organizationApiAssembler.toTeamUserVo(dto));
    }

    /**
     * 更新团队成员关系
     *
     * @param teamUserId 团队成员关系ID
     * @param request    更新请求
     * @return 团队成员关系
     */
    @PutMapping("/team-users/{teamUserId}")
    public SingleResponse<TeamUserVO> updateTeamUser(@PathVariable @Positive(message = "团队成员关系ID必须大于0") Long teamUserId,
                                                     @Valid @RequestBody TeamUserUpdateRequest request) {
        UpdateTeamUserCommand command = organizationApiAssembler.toUpdateTeamUserCommand(request);
        command.setTeamUserId(teamUserId);
        TeamUserDTO dto = organizationApplicationService.updateTeamUser(command);
        return SingleResponse.of(organizationApiAssembler.toTeamUserVo(dto));
    }

    /**
     * 删除团队成员关系
     *
     * @param teamUserId 团队成员关系ID
     * @return 操作结果
     */
    @DeleteMapping("/team-users/{teamUserId}")
    public SingleResponse<Void> deleteTeamUser(@PathVariable @Positive(message = "团队成员关系ID必须大于0") Long teamUserId) {
        organizationApplicationService.deleteTeamUser(teamUserId);
        return SingleResponse.of(null);
    }
}
