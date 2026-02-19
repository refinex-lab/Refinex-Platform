package cn.refinex.system.application.service;

import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.PageResponse;
import cn.refinex.base.utils.PageUtils;
import cn.refinex.system.application.assembler.SystemDomainAssembler;
import cn.refinex.system.application.command.*;
import cn.refinex.system.application.dto.*;
import cn.refinex.system.domain.error.SystemErrorCode;
import cn.refinex.system.domain.model.entity.MenuEntity;
import cn.refinex.system.domain.model.entity.MenuOpEntity;
import cn.refinex.system.domain.model.entity.RoleEntity;
import cn.refinex.system.domain.model.entity.SystemEntity;
import cn.refinex.system.domain.repository.SystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static cn.refinex.base.utils.ValueUtils.defaultIfNull;
import static cn.refinex.base.utils.ValueUtils.isBlank;
import static cn.refinex.base.utils.ValueUtils.trimToNull;

/**
 * 系统管理应用服务
 *
 * @author refinex
 */
@Service
@RequiredArgsConstructor
public class SystemApplicationService {

    private final SystemRepository systemRepository;
    private final SystemDomainAssembler systemDomainAssembler;

    /**
     * 查询系统列表
     *
     * @param command 查询命令
     * @return 系统列表
     */
    public PageResponse<SystemDTO> listSystems(QuerySystemListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);
        PageResponse<SystemEntity> entities = systemRepository.listSystems(
                command == null ? null : command.getStatus(),
                command == null ? null : command.getKeyword(),
                currentPage,
                pageSize
        );

        List<SystemDTO> result = new ArrayList<>();
        for (SystemEntity entity : entities.getData()) {
            result.add(systemDomainAssembler.toSystemDto(entity));
        }
        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询系统详情
     *
     * @param systemId 系统ID
     * @return 系统详情
     */
    public SystemDTO getSystem(Long systemId) {
        if (systemId == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        SystemEntity system = requireSystem(systemId);
        return systemDomainAssembler.toSystemDto(system);
    }

    /**
     * 创建系统
     *
     * @param command 创建命令
     * @return 系统详情
     */
    @Transactional(rollbackFor = Exception.class)
    public SystemDTO createSystem(CreateSystemCommand command) {
        if (command == null
                || isBlank(command.getSystemCode())
                || isBlank(command.getSystemName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        String systemCode = command.getSystemCode().trim();
        if (systemRepository.countSystemCode(systemCode, null) > 0) {
            throw new BizException(SystemErrorCode.SYSTEM_CODE_DUPLICATED);
        }

        SystemEntity entity = new SystemEntity();
        entity.setSystemCode(systemCode);
        entity.setSystemName(command.getSystemName().trim());
        entity.setSystemType(defaultIfNull(command.getSystemType(), 0));
        entity.setBaseUrl(trimToNull(command.getBaseUrl()));
        entity.setStatus(defaultIfNull(command.getStatus(), 1));
        entity.setSort(defaultIfNull(command.getSort(), 0));
        entity.setRemark(trimToNull(command.getRemark()));

        SystemEntity created = systemRepository.insertSystem(entity);
        return systemDomainAssembler.toSystemDto(created);
    }

    /**
     * 更新系统
     *
     * @param command 更新命令
     * @return 系统详情
     */
    @Transactional(rollbackFor = Exception.class)
    public SystemDTO updateSystem(UpdateSystemCommand command) {
        if (command == null
                || command.getSystemId() == null
                || isBlank(command.getSystemName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        SystemEntity existing = requireSystem(command.getSystemId());

        existing.setSystemName(command.getSystemName().trim());
        existing.setSystemType(defaultIfNull(command.getSystemType(), existing.getSystemType()));
        existing.setBaseUrl(trimToNull(command.getBaseUrl()));
        existing.setStatus(defaultIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(defaultIfNull(command.getSort(), existing.getSort()));
        existing.setRemark(trimToNull(command.getRemark()));

        systemRepository.updateSystem(existing);
        return systemDomainAssembler.toSystemDto(requireSystem(existing.getId()));
    }

    /**
     * 查询角色列表
     *
     * @param command 查询命令
     * @return 角色列表
     */
    public PageResponse<RoleDTO> listRoles(QueryRoleListCommand command) {
        if (command == null || command.getSystemId() == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        requireSystem(command.getSystemId());

        int currentPage = PageUtils.normalizeCurrentPage(command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);

        PageResponse<RoleEntity> entities = systemRepository.listRoles(
                command.getSystemId(),
                command.getEstabId(),
                command.getStatus(),
                command.getKeyword(),
                currentPage,
                pageSize
        );
        List<RoleDTO> result = new ArrayList<>();
        for (RoleEntity entity : entities.getData()) {
            result.add(systemDomainAssembler.toRoleDto(entity));
        }
        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询角色详情
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    public RoleDTO getRole(Long roleId) {
        RoleEntity role = requireRole(roleId);
        return systemDomainAssembler.toRoleDto(role);
    }

    /**
     * 创建角色
     *
     * @param command 创建命令
     * @return 角色详情
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleDTO createRole(CreateRoleCommand command) {
        if (command == null
                || command.getSystemId() == null
                || isBlank(command.getRoleCode())
                || isBlank(command.getRoleName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        requireSystem(command.getSystemId());

        Long estabId = defaultIfNull(command.getEstabId(), 0L);
        String roleCode = command.getRoleCode().trim();
        if (systemRepository.countRoleCode(command.getSystemId(), estabId, roleCode, null) > 0) {
            throw new BizException(SystemErrorCode.ROLE_CODE_DUPLICATED);
        }

        RoleEntity entity = new RoleEntity();
        entity.setSystemId(command.getSystemId());
        entity.setEstabId(estabId);
        entity.setRoleCode(roleCode);
        entity.setRoleName(command.getRoleName().trim());
        entity.setRoleType(defaultIfNull(command.getRoleType(), 2));
        entity.setDataScopeType(defaultIfNull(command.getDataScopeType(), 0));
        entity.setParentRoleId(defaultIfNull(command.getParentRoleId(), 0L));
        entity.setIsBuiltin(defaultIfNull(command.getIsBuiltin(), 0));
        entity.setStatus(defaultIfNull(command.getStatus(), 1));
        entity.setSort(defaultIfNull(command.getSort(), 0));
        entity.setRemark(trimToNull(command.getRemark()));

        RoleEntity created = systemRepository.insertRole(entity);
        return systemDomainAssembler.toRoleDto(created);
    }

    /**
     * 更新角色
     *
     * @param command 更新命令
     * @return 角色详情
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleDTO updateRole(UpdateRoleCommand command) {
        if (command == null || command.getRoleId() == null || isBlank(command.getRoleName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        RoleEntity existing = requireRole(command.getRoleId());
        existing.setRoleName(command.getRoleName().trim());
        existing.setRoleType(defaultIfNull(command.getRoleType(), existing.getRoleType()));
        existing.setDataScopeType(defaultIfNull(command.getDataScopeType(), existing.getDataScopeType()));
        existing.setParentRoleId(defaultIfNull(command.getParentRoleId(), existing.getParentRoleId()));
        existing.setIsBuiltin(defaultIfNull(command.getIsBuiltin(), existing.getIsBuiltin()));
        existing.setStatus(defaultIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(defaultIfNull(command.getSort(), existing.getSort()));
        existing.setRemark(trimToNull(command.getRemark()));
        systemRepository.updateRole(existing);
        return systemDomainAssembler.toRoleDto(requireRole(existing.getId()));
    }

    /**
     * 查询角色授权信息
     *
     * @param roleId 角色ID
     * @return 角色授权信息
     */
    public RoleBindingDTO getRoleBindings(Long roleId) {
        requireRole(roleId);
        RoleBindingDTO dto = new RoleBindingDTO();
        dto.setUserIds(systemRepository.listRoleUserIds(roleId));
        dto.setMenuIds(systemRepository.listRoleMenuIds(roleId));
        dto.setMenuOpIds(systemRepository.listRoleMenuOpIds(roleId));
        return dto;
    }

    /**
     * 角色授权用户
     *
     * @param command 授权命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRoleUsers(AssignRoleUsersCommand command) {
        if (command == null || command.getRoleId() == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        RoleEntity role = requireRole(command.getRoleId());
        List<Long> userIds = normalizeIdList(command.getUserIds());
        systemRepository.replaceRoleUsers(role.getId(), role.getEstabId(), userIds, command.getOperatorUserId());
    }

    /**
     * 角色授权菜单与操作
     *
     * @param command 授权命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRolePermissions(AssignRolePermissionsCommand command) {
        if (command == null || command.getRoleId() == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        RoleEntity role = requireRole(command.getRoleId());

        List<Long> menuIds = normalizeIdList(command.getMenuIds());
        if (!menuIds.isEmpty()) {
            long existingMenuCount = systemRepository.countMenusByIdsAndSystemId(role.getSystemId(), menuIds);
            if (existingMenuCount != menuIds.size()) {
                throw new BizException(SystemErrorCode.MENU_NOT_FOUND);
            }
        }

        List<Long> menuOpIds = normalizeIdList(command.getMenuOpIds());
        if (!menuOpIds.isEmpty()) {
            long existingOpCount = systemRepository.countMenuOpsByIdsAndSystemId(role.getSystemId(), menuOpIds);
            if (existingOpCount != menuOpIds.size()) {
                throw new BizException(SystemErrorCode.MENU_OP_NOT_FOUND);
            }
        }

        systemRepository.replaceRoleMenus(role.getId(), menuIds, menuOpIds, command.getOperatorUserId());
    }

    /**
     * 查询菜单详情
     *
     * @param menuId 菜单ID
     * @return 菜单详情
     */
    public MenuDTO getMenu(Long menuId) {
        MenuEntity menu = requireMenu(menuId);
        return systemDomainAssembler.toMenuDto(menu);
    }

    /**
     * 创建菜单
     *
     * @param command 创建命令
     * @return 菜单详情
     */
    @Transactional(rollbackFor = Exception.class)
    public MenuDTO createMenu(CreateMenuCommand command) {
        if (command == null
                || command.getSystemId() == null
                || isBlank(command.getMenuCode())
                || isBlank(command.getMenuName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        requireSystem(command.getSystemId());

        Long parentId = defaultIfNull(command.getParentId(), 0L);
        if (parentId > 0) {
            MenuEntity parent = requireMenu(parentId);
            if (!Objects.equals(parent.getSystemId(), command.getSystemId())) {
                throw new BizException(SystemErrorCode.INVALID_PARAM);
            }
        }

        String menuCode = command.getMenuCode().trim();
        if (systemRepository.countMenuCode(command.getSystemId(), menuCode, null) > 0) {
            throw new BizException(SystemErrorCode.MENU_CODE_DUPLICATED);
        }

        MenuEntity entity = new MenuEntity();
        entity.setSystemId(command.getSystemId());
        entity.setParentId(parentId);
        entity.setMenuCode(menuCode);
        entity.setMenuName(command.getMenuName().trim());
        entity.setMenuType(defaultIfNull(command.getMenuType(), 1));
        entity.setPath(trimToNull(command.getPath()));
        entity.setComponent(trimToNull(command.getComponent()));
        entity.setPermissionKey(trimToNull(command.getPermissionKey()));
        entity.setIcon(trimToNull(command.getIcon()));
        entity.setVisible(defaultIfNull(command.getVisible(), 1));
        entity.setIsFrame(defaultIfNull(command.getIsFrame(), 0));
        entity.setIsCache(defaultIfNull(command.getIsCache(), 0));
        entity.setStatus(defaultIfNull(command.getStatus(), 1));
        entity.setSort(defaultIfNull(command.getSort(), 0));

        MenuEntity created = systemRepository.insertMenu(entity);
        return systemDomainAssembler.toMenuDto(created);
    }

    /**
     * 更新菜单
     *
     * @param command 更新命令
     * @return 菜单详情
     */
    @Transactional(rollbackFor = Exception.class)
    public MenuDTO updateMenu(UpdateMenuCommand command) {
        if (command == null
                || command.getMenuId() == null
                || isBlank(command.getMenuCode())
                || isBlank(command.getMenuName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        MenuEntity existing = requireMenu(command.getMenuId());
        Long parentId = defaultIfNull(command.getParentId(), existing.getParentId());
        if (parentId == null) {
            parentId = 0L;
        }
        if (Objects.equals(parentId, existing.getId())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        if (parentId > 0) {
            MenuEntity parent = requireMenu(parentId);
            if (!Objects.equals(parent.getSystemId(), existing.getSystemId())) {
                throw new BizException(SystemErrorCode.INVALID_PARAM);
            }
        }

        String menuCode = command.getMenuCode().trim();
        if (systemRepository.countMenuCode(existing.getSystemId(), menuCode, existing.getId()) > 0) {
            throw new BizException(SystemErrorCode.MENU_CODE_DUPLICATED);
        }

        existing.setParentId(parentId);
        existing.setMenuCode(menuCode);
        existing.setMenuName(command.getMenuName().trim());
        existing.setMenuType(defaultIfNull(command.getMenuType(), existing.getMenuType()));
        existing.setPath(trimToNull(command.getPath()));
        existing.setComponent(trimToNull(command.getComponent()));
        existing.setPermissionKey(trimToNull(command.getPermissionKey()));
        existing.setIcon(trimToNull(command.getIcon()));
        existing.setVisible(defaultIfNull(command.getVisible(), existing.getVisible()));
        existing.setIsFrame(defaultIfNull(command.getIsFrame(), existing.getIsFrame()));
        existing.setIsCache(defaultIfNull(command.getIsCache(), existing.getIsCache()));
        existing.setStatus(defaultIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(defaultIfNull(command.getSort(), existing.getSort()));

        systemRepository.updateMenu(existing);
        return systemDomainAssembler.toMenuDto(requireMenu(existing.getId()));
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long menuId) {
        MenuEntity menu = requireMenu(menuId);
        if (systemRepository.countChildMenus(menu.getId()) > 0) {
            throw new BizException(SystemErrorCode.MENU_HAS_CHILDREN);
        }
        if (systemRepository.countMenuOpsByMenuId(menu.getId()) > 0) {
            throw new BizException(SystemErrorCode.MENU_HAS_OPS);
        }
        systemRepository.deleteRoleMenuBindingsByMenuId(menu.getId());
        systemRepository.deleteMenuById(menu.getId());
    }

    /**
     * 查询菜单操作列表
     *
     * @param menuId 菜单ID
     * @return 菜单操作列表
     */
    public PageResponse<MenuOpManageDTO> listMenuOps(Long menuId, int currentPage, int pageSize) {
        requireMenu(menuId);
        PageResponse<MenuOpEntity> entities = systemRepository.listMenuOpsByMenuId(
                menuId,
                PageUtils.normalizeCurrentPage(currentPage),
                PageUtils.normalizePageSize(pageSize, PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE)
        );
        List<MenuOpManageDTO> result = new ArrayList<>();
        for (MenuOpEntity entity : entities.getData()) {
            result.add(systemDomainAssembler.toMenuOpManageDto(entity));
        }
        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询菜单操作详情
     *
     * @param menuOpId 菜单操作ID
     * @return 菜单操作详情
     */
    public MenuOpManageDTO getMenuOp(Long menuOpId) {
        MenuOpEntity menuOp = requireMenuOp(menuOpId);
        return systemDomainAssembler.toMenuOpManageDto(menuOp);
    }

    /**
     * 创建菜单操作
     *
     * @param command 创建命令
     * @return 菜单操作详情
     */
    @Transactional(rollbackFor = Exception.class)
    public MenuOpManageDTO createMenuOp(CreateMenuOpCommand command) {
        if (command == null
                || command.getMenuId() == null
                || isBlank(command.getOpCode())
                || isBlank(command.getOpName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        MenuEntity menu = requireMenu(command.getMenuId());
        String opCode = command.getOpCode().trim();
        if (systemRepository.countMenuOpCode(menu.getId(), opCode, null) > 0) {
            throw new BizException(SystemErrorCode.MENU_OP_CODE_DUPLICATED);
        }

        MenuOpEntity entity = new MenuOpEntity();
        entity.setMenuId(menu.getId());
        entity.setOpCode(opCode);
        entity.setOpName(command.getOpName().trim());
        entity.setHttpMethod(trimToNull(command.getHttpMethod()));
        entity.setPathPattern(trimToNull(command.getPathPattern()));
        entity.setPermissionKey(trimToNull(command.getPermissionKey()));
        entity.setStatus(defaultIfNull(command.getStatus(), 1));
        entity.setSort(defaultIfNull(command.getSort(), 0));

        MenuOpEntity created = systemRepository.insertMenuOp(entity);
        return systemDomainAssembler.toMenuOpManageDto(created);
    }

    /**
     * 更新菜单操作
     *
     * @param command 更新命令
     * @return 菜单操作详情
     */
    @Transactional(rollbackFor = Exception.class)
    public MenuOpManageDTO updateMenuOp(UpdateMenuOpCommand command) {
        if (command == null
                || command.getMenuOpId() == null
                || isBlank(command.getOpCode())
                || isBlank(command.getOpName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        MenuOpEntity existing = requireMenuOp(command.getMenuOpId());
        String opCode = command.getOpCode().trim();
        if (systemRepository.countMenuOpCode(existing.getMenuId(), opCode, existing.getId()) > 0) {
            throw new BizException(SystemErrorCode.MENU_OP_CODE_DUPLICATED);
        }

        existing.setOpCode(opCode);
        existing.setOpName(command.getOpName().trim());
        existing.setHttpMethod(trimToNull(command.getHttpMethod()));
        existing.setPathPattern(trimToNull(command.getPathPattern()));
        existing.setPermissionKey(trimToNull(command.getPermissionKey()));
        existing.setStatus(defaultIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(defaultIfNull(command.getSort(), existing.getSort()));

        systemRepository.updateMenuOp(existing);
        return systemDomainAssembler.toMenuOpManageDto(requireMenuOp(existing.getId()));
    }

    /**
     * 删除菜单操作
     *
     * @param menuOpId 菜单操作ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenuOp(Long menuOpId) {
        MenuOpEntity menuOp = requireMenuOp(menuOpId);
        systemRepository.deleteRoleMenuOpBindingsByMenuOpId(menuOp.getId());
        systemRepository.deleteMenuOpById(menuOp.getId());
    }

    /**
     * 按系统查询菜单树
     *
     * @param command 查询命令
     * @return 菜单树
     */
    public List<MenuTreeNodeDTO> getMenuTree(QueryMenuTreeCommand command) {
        if (command == null || command.getSystemId() == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        requireSystem(command.getSystemId());

        Set<Long> assignedMenuIds = new LinkedHashSet<>();
        Set<Long> assignedMenuOpIds = new LinkedHashSet<>();
        if (command.getRoleId() != null) {
            RoleEntity role = requireRole(command.getRoleId());
            if (!Objects.equals(role.getSystemId(), command.getSystemId())) {
                throw new BizException(SystemErrorCode.INVALID_PARAM);
            }
            assignedMenuIds.addAll(systemRepository.listRoleMenuIds(role.getId()));
            assignedMenuOpIds.addAll(systemRepository.listRoleMenuOpIds(role.getId()));
        }

        List<MenuEntity> menuEntities = systemRepository.listMenusBySystemId(command.getSystemId());
        List<MenuOpEntity> menuOpEntities = systemRepository.listMenuOpsBySystemId(command.getSystemId());

        Map<Long, List<MenuOpDTO>> menuOpMap = new LinkedHashMap<>();
        for (MenuOpEntity menuOpEntity : menuOpEntities) {
            MenuOpDTO menuOpDTO = systemDomainAssembler.toMenuOpDto(menuOpEntity);
            menuOpDTO.setAssigned(assignedMenuOpIds.contains(menuOpEntity.getId()));
            menuOpMap.computeIfAbsent(menuOpEntity.getMenuId(), key -> new ArrayList<>()).add(menuOpDTO);
        }
        for (List<MenuOpDTO> ops : menuOpMap.values()) {
            ops.sort(Comparator.comparing(MenuOpDTO::getSort, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(MenuOpDTO::getId));
        }

        Map<Long, MenuTreeNodeDTO> nodeMap = new LinkedHashMap<>();
        menuEntities.sort(Comparator.comparing(MenuEntity::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MenuEntity::getId));
        for (MenuEntity menuEntity : menuEntities) {
            MenuTreeNodeDTO node = systemDomainAssembler.toMenuTreeNodeDto(menuEntity);
            node.setAssigned(assignedMenuIds.contains(menuEntity.getId()));
            node.setOperations(menuOpMap.getOrDefault(menuEntity.getId(), new ArrayList<>()));
            nodeMap.put(menuEntity.getId(), node);
        }

        List<MenuTreeNodeDTO> roots = new ArrayList<>();
        for (MenuTreeNodeDTO node : nodeMap.values()) {
            Long parentId = node.getParentId();
            MenuTreeNodeDTO parent = parentId == null ? null : nodeMap.get(parentId);
            if (parentId == null || parentId == 0 || parent == null) {
                roots.add(node);
                continue;
            }
            parent.getChildren().add(node);
        }
        return roots;
    }

    /**
     * 获取系统
     *
     * @param systemId 系统ID
     * @return 系统
     */
    private SystemEntity requireSystem(Long systemId) {
        if (systemId == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        SystemEntity system = systemRepository.findSystemById(systemId);
        if (system == null || (system.getDeleted() != null && system.getDeleted() == 1)) {
            throw new BizException(SystemErrorCode.SYSTEM_NOT_FOUND);
        }
        return system;
    }

    /**
     * 获取角色
     *
     * @param roleId 角色ID
     * @return 角色
     */
    private RoleEntity requireRole(Long roleId) {
        if (roleId == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        RoleEntity role = systemRepository.findRoleById(roleId);
        if (role == null || (role.getDeleted() != null && role.getDeleted() == 1)) {
            throw new BizException(SystemErrorCode.ROLE_NOT_FOUND);
        }
        return role;
    }

    /**
     * 获取菜单
     *
     * @param menuId 菜单ID
     * @return 菜单
     */
    private MenuEntity requireMenu(Long menuId) {
        if (menuId == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        MenuEntity menu = systemRepository.findMenuById(menuId);
        if (menu == null) {
            throw new BizException(SystemErrorCode.MENU_NOT_FOUND);
        }
        return menu;
    }

    /**
     * 获取菜单操作
     *
     * @param menuOpId 菜单操作ID
     * @return 菜单操作
     */
    private MenuOpEntity requireMenuOp(Long menuOpId) {
        if (menuOpId == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        MenuOpEntity menuOp = systemRepository.findMenuOpById(menuOpId);
        if (menuOp == null) {
            throw new BizException(SystemErrorCode.MENU_OP_NOT_FOUND);
        }
        return menuOp;
    }

    /**
     * 规范化ID列表
     *
     * @param ids ID列表
     * @return 规范化后的ID列表
     */
    private List<Long> normalizeIdList(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        LinkedHashSet<Long> normalized = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id == null || id <= 0) {
                throw new BizException(SystemErrorCode.INVALID_PARAM);
            }
            normalized.add(id);
        }
        return new ArrayList<>(normalized);
    }
}
