package cn.refinex.system.interfaces.assembler;

import cn.refinex.api.user.model.dto.*;
import cn.refinex.system.interfaces.dto.*;
import cn.refinex.system.interfaces.vo.SystemUserEstabVO;
import cn.refinex.system.interfaces.vo.SystemUserIdentityVO;
import cn.refinex.system.interfaces.vo.SystemUserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 系统用户管理接口装配器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface UserManageApiAssembler {

    /**
     * 系统用户列表查询参数转换
     *
     * @param query 查询参数
     * @return 用户管理列表查询参数
     */
    @Mapping(target = "userIds", ignore = true)
    UserManageListQuery toUserManageListQuery(SystemUserListQuery query);

    /**
     * 系统用户创建请求转换
     *
     * @param request 创建请求
     * @return 用户管理创建命令
     */
    UserManageCreateCommand toUserManageCreateCommand(SystemUserCreateRequest request);

    /**
     * 系统用户更新请求转换
     *
     * @param request 更新请求
     * @return 用户管理更新命令
     */
    @Mapping(target = "userId", ignore = true)
    UserManageUpdateCommand toUserManageUpdateCommand(SystemUserUpdateRequest request);

    /**
     * 系统用户身份创建请求转换
     *
     * @param request 创建请求
     * @return 用户身份创建命令
     */
    UserIdentityManageCreateCommand toUserIdentityManageCreateCommand(SystemUserIdentityCreateRequest request);

    /**
     * 系统用户身份更新请求转换
     *
     * @param request 更新请求
     * @return 用户身份更新命令
     */
    UserIdentityManageUpdateCommand toUserIdentityManageUpdateCommand(SystemUserIdentityUpdateRequest request);

    /**
     * 用户管理 DTO 转换为系统用户 VO
     *
     * @param dto 用户管理 DTO
     * @return 系统用户 VO
     */
    SystemUserVO toSystemUserVo(UserManageDTO dto);

    /**
     * 用户管理 DTO 列表转换为系统用户 VO 列表
     *
     * @param dtos 用户管理 DTO 列表
     * @return 系统用户 VO 列表
     */
    List<SystemUserVO> toSystemUserVoList(List<UserManageDTO> dtos);

    /**
     * 用户身份 DTO 转换为系统用户身份 VO
     *
     * @param dto 用户身份 DTO
     * @return 系统用户身份 VO
     */
    SystemUserIdentityVO toSystemUserIdentityVo(UserIdentityManageDTO dto);

    /**
     * 用户身份 DTO 列表转换为系统用户身份 VO 列表
     *
     * @param dtos 用户身份 DTO 列表
     * @return 系统用户身份 VO 列表
     */
    List<SystemUserIdentityVO> toSystemUserIdentityVoList(List<UserIdentityManageDTO> dtos);

    /**
     * 用户企业 DTO 转换为系统用户企业 VO
     *
     * @param dto 用户企业 DTO
     * @return 系统用户企业 VO
     */
    SystemUserEstabVO toSystemUserEstabVo(UserManageEstabDTO dto);

    /**
     * 用户企业 DTO 列表转换为系统用户企业 VO 列表
     *
     * @param dtos 用户企业 DTO 列表
     * @return 系统用户企业 VO 列表
     */
    List<SystemUserEstabVO> toSystemUserEstabVoList(List<UserManageEstabDTO> dtos);
}
