package cn.refinex.system.application.assembler;

import cn.refinex.system.application.dto.*;
import cn.refinex.system.domain.model.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 系统管理领域装配器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface SystemDomainAssembler {

    /**
     * 系统实体转换为系统DTO
     *
     * @param systemEntity 系统实体
     * @return 系统DTO
     */
    SystemDTO toSystemDto(SystemEntity systemEntity);

    /**
     * 角色实体转换为角色DTO
     *
     * @param roleEntity 角色实体
     * @return 角色DTO
     */
    RoleDTO toRoleDto(RoleEntity roleEntity);

    /**
     * 菜单实体转换为菜单DTO
     *
     * @param menuEntity 菜单实体
     * @return 菜单DTO
     */
    MenuDTO toMenuDto(MenuEntity menuEntity);

    /**
     * 值集实体转换为值集DTO
     *
     * @param valueSetEntity 值集实体
     * @return 值集DTO
     */
    ValueSetDTO toValueSetDto(ValueSetEntity valueSetEntity);

    /**
     * 值集明细实体转换为值集明细DTO
     *
     * @param valueEntity 值集明细实体
     * @return 值集明细DTO
     */
    ValueDTO toValueDto(ValueEntity valueEntity);

    /**
     * 数据资源实体转换为数据资源DTO
     *
     * @param drsEntity 数据资源实体
     * @return 数据资源DTO
     */
    DrsDTO toDrsDto(DrsEntity drsEntity);

    /**
     * 数据资源接口实体转换为数据资源接口DTO
     *
     * @param drsInterfaceEntity 数据资源接口实体
     * @return 数据资源接口DTO
     */
    DrsInterfaceDTO toDrsInterfaceDto(DrsInterfaceEntity drsInterfaceEntity);

    /**
     * 登录日志实体转换为登录日志DTO
     *
     * @param loginLogEntity 登录日志实体
     * @return 登录日志DTO
     */
    LoginLogDTO toLoginLogDto(LoginLogEntity loginLogEntity);

    /**
     * 操作日志实体转换为操作日志DTO
     *
     * @param operateLogEntity 操作日志实体
     * @return 操作日志DTO
     */
    OperateLogDTO toOperateLogDto(OperateLogEntity operateLogEntity);

    /**
     * 错误日志实体转换为错误日志DTO
     *
     * @param errorLogEntity 错误日志实体
     * @return 错误日志DTO
     */
    ErrorLogDTO toErrorLogDto(ErrorLogEntity errorLogEntity);

    /**
     * 通知日志实体转换为通知日志DTO
     *
     * @param notifyLogEntity 通知日志实体
     * @return 通知日志DTO
     */
    NotifyLogDTO toNotifyLogDto(NotifyLogEntity notifyLogEntity);

    /**
     * 菜单操作实体转换为菜单操作管理DTO
     *
     * @param menuOpEntity 菜单操作实体
     * @return 菜单操作管理DTO
     */
    MenuOpManageDTO toMenuOpManageDto(MenuOpEntity menuOpEntity);

    /**
     * 菜单实体转换为菜单树节点DTO
     *
     * @param menuEntity 菜单实体
     * @return 菜单树节点DTO
     */
    @Mapping(target = "assigned", ignore = true)
    @Mapping(target = "operations", ignore = true)
    @Mapping(target = "children", ignore = true)
    MenuTreeNodeDTO toMenuTreeNodeDto(MenuEntity menuEntity);

    /**
     * 菜单操作实体转换为菜单操作DTO
     *
     * @param menuOpEntity 菜单操作实体
     * @return 菜单操作DTO
     */
    @Mapping(target = "assigned", ignore = true)
    MenuOpDTO toMenuOpDto(MenuOpEntity menuOpEntity);
}
