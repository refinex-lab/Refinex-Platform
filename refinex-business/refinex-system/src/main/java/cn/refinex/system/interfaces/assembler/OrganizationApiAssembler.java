package cn.refinex.system.interfaces.assembler;

import cn.refinex.system.application.command.*;
import cn.refinex.system.application.dto.*;
import cn.refinex.system.interfaces.dto.*;
import cn.refinex.system.interfaces.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 企业与组织结构接口装配器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface OrganizationApiAssembler {

    /**
     * 企业列表查询参数转换
     *
     * @param query 查询参数
     * @return 查询命令
     */
    QueryEstabListCommand toQueryEstabListCommand(EstabListQuery query);

    /**
     * 企业创建请求转换
     *
     * @param request 创建请求
     * @return 创建命令
     */
    CreateEstabCommand toCreateEstabCommand(EstabCreateRequest request);

    /**
     * 企业更新请求转换
     *
     * @param request 更新请求
     * @return 更新命令
     */
    @Mapping(target = "estabId", ignore = true)
    UpdateEstabCommand toUpdateEstabCommand(EstabUpdateRequest request);

    /**
     * 企业地址创建请求转换
     *
     * @param request 创建请求
     * @return 创建命令
     */
    @Mapping(target = "estabId", ignore = true)
    CreateEstabAddressCommand toCreateEstabAddressCommand(EstabAddressCreateRequest request);

    /**
     * 企业地址更新请求转换
     *
     * @param request 更新请求
     * @return 更新命令
     */
    @Mapping(target = "addressId", ignore = true)
    UpdateEstabAddressCommand toUpdateEstabAddressCommand(EstabAddressUpdateRequest request);

    /**
     * 企业认证策略更新请求转换
     *
     * @param request 更新请求
     * @return 更新命令
     */
    @Mapping(target = "estabId", ignore = true)
    UpdateEstabAuthPolicyCommand toUpdateEstabAuthPolicyCommand(EstabAuthPolicyUpdateRequest request);

    /**
     * 企业成员创建请求转换
     *
     * @param request 创建请求
     * @return 创建命令
     */
    @Mapping(target = "estabId", ignore = true)
    CreateEstabUserCommand toCreateEstabUserCommand(EstabUserCreateRequest request);

    /**
     * 企业成员更新请求转换
     *
     * @param request 更新请求
     * @return 更新命令
     */
    @Mapping(target = "estabUserId", ignore = true)
    UpdateEstabUserCommand toUpdateEstabUserCommand(EstabUserUpdateRequest request);

    /**
     * 团队列表查询参数转换
     *
     * @param query 查询参数
     * @return 查询命令
     */
    QueryTeamListCommand toQueryTeamListCommand(TeamListQuery query);

    /**
     * 团队创建请求转换
     *
     * @param request 创建请求
     * @return 创建命令
     */
    CreateTeamCommand toCreateTeamCommand(TeamCreateRequest request);

    /**
     * 团队更新请求转换
     *
     * @param request 更新请求
     * @return 更新命令
     */
    @Mapping(target = "teamId", ignore = true)
    UpdateTeamCommand toUpdateTeamCommand(TeamUpdateRequest request);

    /**
     * 团队成员创建请求转换
     *
     * @param request 创建请求
     * @return 创建命令
     */
    @Mapping(target = "teamId", ignore = true)
    CreateTeamUserCommand toCreateTeamUserCommand(TeamUserCreateRequest request);

    /**
     * 团队成员更新请求转换
     *
     * @param request 更新请求
     * @return 更新命令
     */
    @Mapping(target = "teamUserId", ignore = true)
    UpdateTeamUserCommand toUpdateTeamUserCommand(TeamUserUpdateRequest request);

    /**
     * 企业 DTO 转换为 VO
     *
     * @param dto 企业 DTO
     * @return 企业 VO
     */
    EstabVO toEstabVo(EstabDTO dto);

    /**
     * 企业 DTO 列表转换为 VO 列表
     *
     * @param dtos 企业 DTO 列表
     * @return 企业 VO 列表
     */
    List<EstabVO> toEstabVoList(List<EstabDTO> dtos);

    /**
     * 企业地址 DTO 转换为 VO
     *
     * @param dto 企业地址 DTO
     * @return 企业地址 VO
     */
    EstabAddressVO toEstabAddressVo(EstabAddressDTO dto);

    /**
     * 企业地址 DTO 列表转换为 VO 列表
     *
     * @param dtos 企业地址 DTO 列表
     * @return 企业地址 VO 列表
     */
    List<EstabAddressVO> toEstabAddressVoList(List<EstabAddressDTO> dtos);

    /**
     * 企业认证策略 DTO 转换为 VO
     *
     * @param dto 企业认证策略 DTO
     * @return 企业认证策略 VO
     */
    EstabAuthPolicyVO toEstabAuthPolicyVo(EstabAuthPolicyDTO dto);

    /**
     * 企业成员 DTO 列表转换为 VO 列表
     *
     * @param dtos 企业成员 DTO 列表
     * @return 企业成员 VO 列表
     */
    List<EstabUserVO> toEstabUserVoList(List<EstabUserDTO> dtos);

    /**
     * 企业成员 DTO 转换为 VO
     *
     * @param dto 企业成员 DTO
     * @return 企业成员 VO
     */
    EstabUserVO toEstabUserVo(EstabUserDTO dto);

    /**
     * 团队 DTO 列表转换为 VO 列表
     *
     * @param dtos 团队 DTO 列表
     * @return 团队 VO 列表
     */
    List<TeamVO> toTeamVoList(List<TeamDTO> dtos);

    /**
     * 团队 DTO 转换为 VO
     *
     * @param dto 团队 DTO
     * @return 团队 VO
     */
    TeamVO toTeamVo(TeamDTO dto);

    /**
     * 团队成员 DTO 列表转换为 VO 列表
     *
     * @param dtos 团队成员 DTO 列表
     * @return 团队成员 VO 列表
     */
    List<TeamUserVO> toTeamUserVoList(List<TeamUserDTO> dtos);

    /**
     * 团队成员 DTO 转换为 VO
     *
     * @param dto 团队成员 DTO
     * @return 团队成员 VO
     */
    TeamUserVO toTeamUserVo(TeamUserDTO dto);
}
