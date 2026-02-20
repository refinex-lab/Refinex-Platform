package cn.refinex.system.interfaces.assembler;

import cn.refinex.system.application.command.*;
import cn.refinex.system.application.dto.*;
import cn.refinex.system.interfaces.dto.*;
import cn.refinex.system.interfaces.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 系统管理接口装配器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface SystemApiAssembler {

    /**
     * 查询系统列表参数转换
     *
     * @param query 查询系统列表参数
     * @return 查询系统列表命令
     */
    QuerySystemListCommand toQuerySystemListCommand(SystemListQuery query);

    /**
     * 创建系统参数转换
     *
     * @param request 创建系统参数
     * @return 创建系统命令
     */
    CreateSystemCommand toCreateSystemCommand(SystemCreateRequest request);

    /**
     * 更新系统参数转换
     *
     * @param request 更新系统参数
     * @return 更新系统命令
     */
    @Mapping(target = "systemId", ignore = true)
    UpdateSystemCommand toUpdateSystemCommand(SystemUpdateRequest request);

    /**
     * 查询角色列表参数转换
     *
     * @param query 查询角色列表参数
     * @return 查询角色列表命令
     */
    QueryRoleListCommand toQueryRoleListCommand(RoleListQuery query);

    /**
     * 创建角色参数转换
     *
     * @param request 创建角色参数
     * @return 创建角色命令
     */
    CreateRoleCommand toCreateRoleCommand(RoleCreateRequest request);

    /**
     * 更新角色参数转换
     *
     * @param request 更新角色参数
     * @return 更新角色命令
     */
    @Mapping(target = "roleId", ignore = true)
    UpdateRoleCommand toUpdateRoleCommand(RoleUpdateRequest request);

    /**
     * 角色授权用户参数转换
     *
     * @param request 角色授权用户参数
     * @return 角色授权用户命令
     */
    @Mapping(target = "roleId", ignore = true)
    @Mapping(target = "operatorUserId", ignore = true)
    AssignRoleUsersCommand toAssignRoleUsersCommand(AssignRoleUsersRequest request);

    /**
     * 角色授权权限参数转换
     *
     * @param request 角色授权权限参数
     * @return 角色授权权限命令
     */
    @Mapping(target = "roleId", ignore = true)
    @Mapping(target = "operatorUserId", ignore = true)
    AssignRolePermissionsCommand toAssignRolePermissionsCommand(AssignRolePermissionsRequest request);

    /**
     * 创建菜单参数转换
     *
     * @param request 创建菜单参数
     * @return 创建菜单命令
     */
    CreateMenuCommand toCreateMenuCommand(MenuCreateRequest request);

    /**
     * 更新菜单参数转换
     *
     * @param request 更新菜单参数
     * @return 更新菜单命令
     */
    @Mapping(target = "menuId", ignore = true)
    UpdateMenuCommand toUpdateMenuCommand(MenuUpdateRequest request);

    /**
     * 创建菜单操作参数转换
     *
     * @param request 创建菜单操作参数
     * @return 创建菜单操作命令
     */
    @Mapping(target = "menuId", ignore = true)
    CreateMenuOpCommand toCreateMenuOpCommand(MenuOpCreateRequest request);

    /**
     * 更新菜单操作参数转换
     *
     * @param request 更新菜单操作参数
     * @return 更新菜单操作命令
     */
    @Mapping(target = "menuOpId", ignore = true)
    UpdateMenuOpCommand toUpdateMenuOpCommand(MenuOpUpdateRequest request);

    /**
     * 查询值集列表参数转换
     *
     * @param query 查询值集列表参数
     * @return 查询值集列表命令
     */
    QueryValueSetListCommand toQueryValueSetListCommand(ValueSetListQuery query);

    /**
     * 创建值集参数转换
     *
     * @param request 创建值集参数
     * @return 创建值集命令
     */
    CreateValueSetCommand toCreateValueSetCommand(ValueSetCreateRequest request);

    /**
     * 更新值集参数转换
     *
     * @param request 更新值集参数
     * @return 更新值集命令
     */
    @Mapping(target = "valueSetId", ignore = true)
    UpdateValueSetCommand toUpdateValueSetCommand(ValueSetUpdateRequest request);

    /**
     * 查询值集明细列表参数转换
     *
     * @param query 查询值集明细列表参数
     * @return 查询值集明细列表命令
     */
    QueryValueListCommand toQueryValueListCommand(ValueListQuery query);

    /**
     * 创建值集明细参数转换
     *
     * @param request 创建值集明细参数
     * @return 创建值集明细命令
     */
    @Mapping(target = "setCode", ignore = true)
    CreateValueCommand toCreateValueCommand(ValueCreateRequest request);

    /**
     * 更新值集明细参数转换
     *
     * @param request 更新值集明细参数
     * @return 更新值集明细命令
     */
    @Mapping(target = "valueId", ignore = true)
    UpdateValueCommand toUpdateValueCommand(ValueUpdateRequest request);

    /**
     * 查询数据资源列表参数转换
     *
     * @param query 查询数据资源列表参数
     * @return 查询数据资源列表命令
     */
    QueryDrsListCommand toQueryDrsListCommand(DrsListQuery query);

    /**
     * 创建数据资源参数转换
     *
     * @param request 创建数据资源参数
     * @return 创建数据资源命令
     */
    CreateDrsCommand toCreateDrsCommand(DrsCreateRequest request);

    /**
     * 更新数据资源参数转换
     *
     * @param request 更新数据资源参数
     * @return 更新数据资源命令
     */
    @Mapping(target = "drsId", ignore = true)
    UpdateDrsCommand toUpdateDrsCommand(DrsUpdateRequest request);

    /**
     * 查询数据资源接口列表参数转换
     *
     * @param query 查询数据资源接口列表参数
     * @return 查询数据资源接口列表命令
     */
    QueryDrsInterfaceListCommand toQueryDrsInterfaceListCommand(DrsInterfaceListQuery query);

    /**
     * 创建数据资源接口参数转换
     *
     * @param request 创建数据资源接口参数
     * @return 创建数据资源接口命令
     */
    @Mapping(target = "drsId", ignore = true)
    CreateDrsInterfaceCommand toCreateDrsInterfaceCommand(DrsInterfaceCreateRequest request);

    /**
     * 更新数据资源接口参数转换
     *
     * @param request 更新数据资源接口参数
     * @return 更新数据资源接口命令
     */
    @Mapping(target = "interfaceId", ignore = true)
    UpdateDrsInterfaceCommand toUpdateDrsInterfaceCommand(DrsInterfaceUpdateRequest request);

    /**
     * 查询登录日志列表参数转换
     *
     * @param query 查询登录日志列表参数
     * @return 查询登录日志列表命令
     */
    QueryLoginLogListCommand toQueryLoginLogListCommand(LoginLogListQuery query);

    /**
     * 查询操作日志列表参数转换
     *
     * @param query 查询操作日志列表参数
     * @return 查询操作日志列表命令
     */
    QueryOperateLogListCommand toQueryOperateLogListCommand(OperateLogListQuery query);

    /**
     * 查询错误日志列表参数转换
     *
     * @param query 查询错误日志列表参数
     * @return 查询错误日志列表命令
     */
    QueryErrorLogListCommand toQueryErrorLogListCommand(ErrorLogListQuery query);

    /**
     * 查询通知日志列表参数转换
     *
     * @param query 查询通知日志列表参数
     * @return 查询通知日志列表命令
     */
    QueryNotifyLogListCommand toQueryNotifyLogListCommand(NotifyLogListQuery query);

    /**
     * 查询菜单树参数转换
     *
     * @param query 查询菜单树参数
     * @return 查询菜单树命令
     */
    QueryMenuTreeCommand toQueryMenuTreeCommand(MenuTreeQuery query);

    /**
     * 系统DTO转换为系统VO
     *
     * @param dto 系统DTO
     * @return 系统VO
     */
    SystemVO toSystemVo(SystemDTO dto);

    /**
     * 系统DTO列表转换为系统VO列表
     *
     * @param dtos 系统DTO列表
     * @return 系统VO列表
     */
    List<SystemVO> toSystemVoList(List<SystemDTO> dtos);

    /**
     * 角色DTO转换为角色VO
     *
     * @param dto 角色DTO
     * @return 角色VO
     */
    RoleVO toRoleVo(RoleDTO dto);

    /**
     * 角色DTO列表转换为角色VO列表
     *
     * @param dtos 角色DTO列表
     * @return 角色VO列表
     */
    List<RoleVO> toRoleVoList(List<RoleDTO> dtos);

    /**
     * 角色授权信息DTO转换为角色授权信息VO
     *
     * @param dto 角色授权信息DTO
     * @return 角色授权信息VO
     */
    RoleBindingVO toRoleBindingVo(RoleBindingDTO dto);

    /**
     * 角色绑定用户DTO转换为角色绑定用户VO
     *
     * @param dto 角色绑定用户DTO
     * @return 角色绑定用户VO
     */
    RoleBindingUserVO toRoleBindingUserVo(RoleBindingUserDTO dto);

    /**
     * 角色绑定用户DTO列表转换为角色绑定用户VO列表
     *
     * @param dtos 角色绑定用户DTO列表
     * @return 角色绑定用户VO列表
     */
    List<RoleBindingUserVO> toRoleBindingUserVoList(List<RoleBindingUserDTO> dtos);

    /**
     * 菜单DTO转换为菜单VO
     *
     * @param dto 菜单DTO
     * @return 菜单VO
     */
    MenuVO toMenuVo(MenuDTO dto);

    /**
     * 菜单操作管理DTO转换为菜单操作管理VO
     *
     * @param dto 菜单操作管理DTO
     * @return 菜单操作管理VO
     */
    MenuOpManageVO toMenuOpManageVo(MenuOpManageDTO dto);

    /**
     * 菜单操作管理DTO列表转换为菜单操作管理VO列表
     *
     * @param dtos 菜单操作管理DTO列表
     * @return 菜单操作管理VO列表
     */
    List<MenuOpManageVO> toMenuOpManageVoList(List<MenuOpManageDTO> dtos);

    /**
     * 值集DTO转换为值集VO
     *
     * @param dto 值集DTO
     * @return 值集VO
     */
    ValueSetVO toValueSetVo(ValueSetDTO dto);

    /**
     * 值集DTO列表转换为值集VO列表
     *
     * @param dtos 值集DTO列表
     * @return 值集VO列表
     */
    List<ValueSetVO> toValueSetVoList(List<ValueSetDTO> dtos);

    /**
     * 值集明细DTO转换为值集明细VO
     *
     * @param dto 值集明细DTO
     * @return 值集明细VO
     */
    ValueVO toValueVo(ValueDTO dto);

    /**
     * 值集明细DTO列表转换为值集明细VO列表
     *
     * @param dtos 值集明细DTO列表
     * @return 值集明细VO列表
     */
    List<ValueVO> toValueVoList(List<ValueDTO> dtos);

    /**
     * 数据资源DTO转换为数据资源VO
     *
     * @param dto 数据资源DTO
     * @return 数据资源VO
     */
    DrsVO toDrsVo(DrsDTO dto);

    /**
     * 数据资源DTO列表转换为数据资源VO列表
     *
     * @param dtos 数据资源DTO列表
     * @return 数据资源VO列表
     */
    List<DrsVO> toDrsVoList(List<DrsDTO> dtos);

    /**
     * 数据资源接口DTO转换为数据资源接口VO
     *
     * @param dto 数据资源接口DTO
     * @return 数据资源接口VO
     */
    DrsInterfaceVO toDrsInterfaceVo(DrsInterfaceDTO dto);

    /**
     * 数据资源接口DTO列表转换为数据资源接口VO列表
     *
     * @param dtos 数据资源接口DTO列表
     * @return 数据资源接口VO列表
     */
    List<DrsInterfaceVO> toDrsInterfaceVoList(List<DrsInterfaceDTO> dtos);

    /**
     * 登录日志DTO转换为登录日志VO
     *
     * @param dto 登录日志DTO
     * @return 登录日志VO
     */
    LoginLogVO toLoginLogVo(LoginLogDTO dto);

    /**
     * 登录日志DTO列表转换为登录日志VO列表
     *
     * @param dtos 登录日志DTO列表
     * @return 登录日志VO列表
     */
    List<LoginLogVO> toLoginLogVoList(List<LoginLogDTO> dtos);

    /**
     * 操作日志DTO转换为操作日志VO
     *
     * @param dto 操作日志DTO
     * @return 操作日志VO
     */
    OperateLogVO toOperateLogVo(OperateLogDTO dto);

    /**
     * 操作日志DTO列表转换为操作日志VO列表
     *
     * @param dtos 操作日志DTO列表
     * @return 操作日志VO列表
     */
    List<OperateLogVO> toOperateLogVoList(List<OperateLogDTO> dtos);

    /**
     * 错误日志DTO转换为错误日志VO
     *
     * @param dto 错误日志DTO
     * @return 错误日志VO
     */
    ErrorLogVO toErrorLogVo(ErrorLogDTO dto);

    /**
     * 错误日志DTO列表转换为错误日志VO列表
     *
     * @param dtos 错误日志DTO列表
     * @return 错误日志VO列表
     */
    List<ErrorLogVO> toErrorLogVoList(List<ErrorLogDTO> dtos);

    /**
     * 通知日志DTO转换为通知日志VO
     *
     * @param dto 通知日志DTO
     * @return 通知日志VO
     */
    NotifyLogVO toNotifyLogVo(NotifyLogDTO dto);

    /**
     * 通知日志DTO列表转换为通知日志VO列表
     *
     * @param dtos 通知日志DTO列表
     * @return 通知日志VO列表
     */
    List<NotifyLogVO> toNotifyLogVoList(List<NotifyLogDTO> dtos);

    /**
     * 菜单树节点DTO转换为菜单树节点VO
     *
     * @param dto 菜单树节点DTO
     * @return 菜单树节点VO
     */
    MenuTreeNodeVO toMenuTreeNodeVo(MenuTreeNodeDTO dto);

    /**
     * 菜单树节点DTO列表转换为菜单树节点VO列表
     *
     * @param dtos 菜单树节点DTO列表
     * @return 菜单树节点VO列表
     */
    List<MenuTreeNodeVO> toMenuTreeNodeVoList(List<MenuTreeNodeDTO> dtos);
}
