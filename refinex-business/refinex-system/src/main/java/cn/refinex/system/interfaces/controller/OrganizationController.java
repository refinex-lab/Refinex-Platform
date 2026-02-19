package cn.refinex.system.interfaces.controller;

import cn.refinex.base.response.PageResponse;
import cn.refinex.api.user.context.CurrentUserProvider;
import cn.refinex.system.application.command.*;
import cn.refinex.system.application.dto.*;
import cn.refinex.system.application.service.OrganizationApplicationService;
import cn.refinex.system.interfaces.assembler.OrganizationApiAssembler;
import cn.refinex.system.interfaces.dto.*;
import cn.refinex.system.interfaces.vo.*;
import cn.refinex.web.vo.PageResult;
import cn.refinex.web.vo.Result;
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
    private final CurrentUserProvider currentUserProvider;

    /**
     * 查询企业列表
     *
     * @param query 查询参数
     * @return 企业列表
     */
    @GetMapping("/estabs")
    public PageResult<EstabVO> listEstabs(@Valid EstabListQuery query) {
        QueryEstabListCommand command = organizationApiAssembler.toQueryEstabListCommand(query);
        PageResponse<EstabDTO> list = organizationApplicationService.listEstabs(command);
        return PageResult.success(
                organizationApiAssembler.toEstabVoList(list.getData()),
                list.getTotal(),
                list.getCurrentPage(),
                list.getPageSize()
        );
    }

    /**
     * 查询企业详情
     *
     * @param estabId 企业ID
     * @return 企业详情
     */
    @GetMapping("/estabs/{estabId}")
    public Result<EstabVO> getEstab(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId) {
        EstabDTO dto = organizationApplicationService.getEstab(estabId);
        return Result.success(organizationApiAssembler.toEstabVo(dto));
    }

    /**
     * 创建企业
     *
     * @param request 创建请求
     * @return 企业详情
     */
    @PostMapping("/estabs")
    public Result<EstabVO> createEstab(@Valid @RequestBody EstabCreateRequest request) {
        CreateEstabCommand command = organizationApiAssembler.toCreateEstabCommand(request);
        EstabDTO dto = organizationApplicationService.createEstab(command);
        return Result.success(organizationApiAssembler.toEstabVo(dto));
    }

    /**
     * 更新企业
     *
     * @param estabId 企业ID
     * @param request 更新请求
     * @return 企业详情
     */
    @PutMapping("/estabs/{estabId}")
    public Result<EstabVO> updateEstab(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId,
                                       @Valid @RequestBody EstabUpdateRequest request) {
        UpdateEstabCommand command = organizationApiAssembler.toUpdateEstabCommand(request);
        command.setEstabId(estabId);
        EstabDTO dto = organizationApplicationService.updateEstab(command);
        return Result.success(organizationApiAssembler.toEstabVo(dto));
    }

    /**
     * 删除企业
     *
     * @param estabId 企业ID
     * @return 操作结果
     */
    @DeleteMapping("/estabs/{estabId}")
    public Result<Void> deleteEstab(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId) {
        organizationApplicationService.deleteEstab(estabId);
        return Result.success();
    }

    /**
     * 查询企业地址列表
     *
     * @param estabId 企业ID
     * @param query   查询参数
     * @return 企业地址列表
     */
    @GetMapping("/estabs/{estabId}/addresses")
    public PageResult<EstabAddressVO> listEstabAddresses(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId,
                                                         @Valid EstabAddressListQuery query) {
        PageResponse<EstabAddressDTO> list = organizationApplicationService.listEstabAddresses(
                estabId,
                query.getAddrType(),
                query.getCurrentPage(),
                query.getPageSize()
        );
        return PageResult.success(
                organizationApiAssembler.toEstabAddressVoList(list.getData()),
                list.getTotal(),
                list.getCurrentPage(),
                list.getPageSize()
        );
    }

    /**
     * 创建企业地址
     *
     * @param estabId 企业ID
     * @param request 创建请求
     * @return 地址详情
     */
    @PostMapping("/estabs/{estabId}/addresses")
    public Result<EstabAddressVO> createEstabAddress(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId,
                                                     @Valid @RequestBody EstabAddressCreateRequest request) {
        CreateEstabAddressCommand command = organizationApiAssembler.toCreateEstabAddressCommand(request);
        command.setEstabId(estabId);
        EstabAddressDTO dto = organizationApplicationService.createEstabAddress(command);
        return Result.success(organizationApiAssembler.toEstabAddressVo(dto));
    }

    /**
     * 更新企业地址
     *
     * @param addressId 地址ID
     * @param request   更新请求
     * @return 地址详情
     */
    @PutMapping("/estab-addresses/{addressId}")
    public Result<EstabAddressVO> updateEstabAddress(@PathVariable @Positive(message = "地址ID必须大于0") Long addressId,
                                                     @Valid @RequestBody EstabAddressUpdateRequest request) {
        UpdateEstabAddressCommand command = organizationApiAssembler.toUpdateEstabAddressCommand(request);
        command.setAddressId(addressId);
        EstabAddressDTO dto = organizationApplicationService.updateEstabAddress(command);
        return Result.success(organizationApiAssembler.toEstabAddressVo(dto));
    }

    /**
     * 删除企业地址
     *
     * @param addressId 地址ID
     * @return 操作结果
     */
    @DeleteMapping("/estab-addresses/{addressId}")
    public Result<Void> deleteEstabAddress(@PathVariable @Positive(message = "地址ID必须大于0") Long addressId) {
        organizationApplicationService.deleteEstabAddress(addressId);
        return Result.success();
    }

    /**
     * 查询企业认证策略
     *
     * @param estabId 企业ID
     * @return 认证策略
     */
    @GetMapping("/estabs/{estabId}/auth-policy")
    public Result<EstabAuthPolicyVO> getEstabAuthPolicy(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId) {
        EstabAuthPolicyDTO dto = organizationApplicationService.getEstabAuthPolicy(estabId);
        return Result.success(organizationApiAssembler.toEstabAuthPolicyVo(dto));
    }

    /**
     * 更新企业认证策略
     *
     * @param estabId 企业ID
     * @param request 更新请求
     * @return 认证策略
     */
    @PutMapping("/estabs/{estabId}/auth-policy")
    public Result<EstabAuthPolicyVO> updateEstabAuthPolicy(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId,
                                                           @Valid @RequestBody EstabAuthPolicyUpdateRequest request) {
        UpdateEstabAuthPolicyCommand command = organizationApiAssembler.toUpdateEstabAuthPolicyCommand(request);
        command.setEstabId(estabId);
        EstabAuthPolicyDTO dto = organizationApplicationService.updateEstabAuthPolicy(command);
        return Result.success(organizationApiAssembler.toEstabAuthPolicyVo(dto));
    }

    /**
     * 查询企业成员列表
     *
     * @param estabId 企业ID
     * @param query   查询参数
     * @return 企业成员列表
     */
    @GetMapping("/estabs/{estabId}/users")
    public PageResult<EstabUserVO> listEstabUsers(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId,
                                                  @Valid EstabUserListQuery query) {
        PageResponse<EstabUserDTO> list = organizationApplicationService.listEstabUsers(
                estabId,
                query.getStatus(),
                query.getCurrentPage(),
                query.getPageSize()
        );
        return PageResult.success(
                organizationApiAssembler.toEstabUserVoList(list.getData()),
                list.getTotal(),
                list.getCurrentPage(),
                list.getPageSize()
        );
    }

    /**
     * 创建企业成员关系
     *
     * @param estabId 企业ID
     * @param request 创建请求
     * @return 成员关系
     */
    @PostMapping("/estabs/{estabId}/users")
    public Result<EstabUserVO> createEstabUser(@PathVariable @Positive(message = "企业ID必须大于0") Long estabId,
                                               @Valid @RequestBody EstabUserCreateRequest request) {
        CreateEstabUserCommand command = organizationApiAssembler.toCreateEstabUserCommand(request);
        command.setEstabId(estabId);
        EstabUserDTO dto = organizationApplicationService.createEstabUser(command);
        return Result.success(organizationApiAssembler.toEstabUserVo(dto));
    }

    /**
     * 更新企业成员关系
     *
     * @param estabUserId 企业成员关系ID
     * @param request     更新请求
     * @return 成员关系
     */
    @PutMapping("/estab-users/{estabUserId}")
    public Result<EstabUserVO> updateEstabUser(@PathVariable @Positive(message = "企业成员关系ID必须大于0") Long estabUserId,
                                               @Valid @RequestBody EstabUserUpdateRequest request) {
        UpdateEstabUserCommand command = organizationApiAssembler.toUpdateEstabUserCommand(request);
        command.setEstabUserId(estabUserId);
        EstabUserDTO dto = organizationApplicationService.updateEstabUser(command);
        return Result.success(organizationApiAssembler.toEstabUserVo(dto));
    }

    /**
     * 删除企业成员关系
     *
     * @param estabUserId 企业成员关系ID
     * @return 操作结果
     */
    @DeleteMapping("/estab-users/{estabUserId}")
    public Result<Void> deleteEstabUser(@PathVariable @Positive(message = "企业成员关系ID必须大于0") Long estabUserId) {
        organizationApplicationService.deleteEstabUser(estabUserId);
        return Result.success();
    }

    /**
     * 查询团队列表
     *
     * @param query 查询参数
     * @return 团队列表
     */
    @GetMapping("/teams")
    public PageResult<TeamVO> listTeams(@Valid TeamListQuery query) {
        QueryTeamListCommand command = organizationApiAssembler.toQueryTeamListCommand(query);
        PageResponse<TeamDTO> list = organizationApplicationService.listTeams(command);
        return PageResult.success(
                organizationApiAssembler.toTeamVoList(list.getData()),
                list.getTotal(),
                list.getCurrentPage(),
                list.getPageSize()
        );
    }

    /**
     * 查询团队详情
     *
     * @param teamId 团队ID
     * @return 团队详情
     */
    @GetMapping("/teams/{teamId}")
    public Result<TeamVO> getTeam(@PathVariable @Positive(message = "团队ID必须大于0") Long teamId) {
        TeamDTO dto = organizationApplicationService.getTeam(teamId);
        return Result.success(organizationApiAssembler.toTeamVo(dto));
    }

    /**
     * 创建团队
     *
     * @param request 创建请求
     * @return 团队详情
     */
    @PostMapping("/teams")
    public Result<TeamVO> createTeam(@Valid @RequestBody TeamCreateRequest request) {
        CreateTeamCommand command = organizationApiAssembler.toCreateTeamCommand(request);
        command.setEstabId(currentUserProvider.getCurrentEstabId());
        TeamDTO dto = organizationApplicationService.createTeam(command);
        return Result.success(organizationApiAssembler.toTeamVo(dto));
    }

    /**
     * 更新团队
     *
     * @param teamId  团队ID
     * @param request 更新请求
     * @return 团队详情
     */
    @PutMapping("/teams/{teamId}")
    public Result<TeamVO> updateTeam(@PathVariable @Positive(message = "团队ID必须大于0") Long teamId,
                                     @Valid @RequestBody TeamUpdateRequest request) {
        UpdateTeamCommand command = organizationApiAssembler.toUpdateTeamCommand(request);
        command.setTeamId(teamId);
        TeamDTO dto = organizationApplicationService.updateTeam(command);
        return Result.success(organizationApiAssembler.toTeamVo(dto));
    }

    /**
     * 删除团队
     *
     * @param teamId 团队ID
     * @return 操作结果
     */
    @DeleteMapping("/teams/{teamId}")
    public Result<Void> deleteTeam(@PathVariable @Positive(message = "团队ID必须大于0") Long teamId) {
        organizationApplicationService.deleteTeam(teamId);
        return Result.success();
    }

    /**
     * 查询团队成员列表
     *
     * @param teamId 团队ID
     * @param query  查询参数
     * @return 团队成员列表
     */
    @GetMapping("/teams/{teamId}/users")
    public PageResult<TeamUserVO> listTeamUsers(@PathVariable @Positive(message = "团队ID必须大于0") Long teamId,
                                                @Valid TeamUserListQuery query) {
        PageResponse<TeamUserDTO> list = organizationApplicationService.listTeamUsers(
                teamId,
                query.getStatus(),
                query.getCurrentPage(),
                query.getPageSize()
        );
        return PageResult.success(
                organizationApiAssembler.toTeamUserVoList(list.getData()),
                list.getTotal(),
                list.getCurrentPage(),
                list.getPageSize()
        );
    }

    /**
     * 查询团队成员候选用户（用于前端联想）
     *
     * @param teamId 团队ID
     * @param query  查询参数
     * @return 候选用户列表
     */
    @GetMapping("/teams/{teamId}/users/candidates")
    public Result<List<TeamUserCandidateVO>> listTeamUserCandidates(
            @PathVariable @Positive(message = "团队ID必须大于0") Long teamId,
            @Valid TeamUserCandidateQuery query) {
        List<TeamUserCandidateDTO> users = organizationApplicationService.listTeamUserCandidates(
                teamId,
                query.getKeyword(),
                query.getLimit()
        );
        return Result.success(organizationApiAssembler.toTeamUserCandidateVoList(users));
    }

    /**
     * 创建团队成员关系
     *
     * @param teamId  团队ID
     * @param request 创建请求
     * @return 团队成员关系
     */
    @PostMapping("/teams/{teamId}/users")
    public Result<TeamUserVO> createTeamUser(@PathVariable @Positive(message = "团队ID必须大于0") Long teamId,
                                             @Valid @RequestBody TeamUserCreateRequest request) {
        CreateTeamUserCommand command = organizationApiAssembler.toCreateTeamUserCommand(request);
        command.setTeamId(teamId);
        TeamUserDTO dto = organizationApplicationService.createTeamUser(command);
        return Result.success(organizationApiAssembler.toTeamUserVo(dto));
    }

    /**
     * 更新团队成员关系
     *
     * @param teamUserId 团队成员关系ID
     * @param request    更新请求
     * @return 团队成员关系
     */
    @PutMapping("/team-users/{teamUserId}")
    public Result<TeamUserVO> updateTeamUser(@PathVariable @Positive(message = "团队成员关系ID必须大于0") Long teamUserId,
                                             @Valid @RequestBody TeamUserUpdateRequest request) {
        UpdateTeamUserCommand command = organizationApiAssembler.toUpdateTeamUserCommand(request);
        command.setTeamUserId(teamUserId);
        TeamUserDTO dto = organizationApplicationService.updateTeamUser(command);
        return Result.success(organizationApiAssembler.toTeamUserVo(dto));
    }

    /**
     * 删除团队成员关系
     *
     * @param teamUserId 团队成员关系ID
     * @return 操作结果
     */
    @DeleteMapping("/team-users/{teamUserId}")
    public Result<Void> deleteTeamUser(@PathVariable @Positive(message = "团队成员关系ID必须大于0") Long teamUserId) {
        organizationApplicationService.deleteTeamUser(teamUserId);
        return Result.success();
    }
}
