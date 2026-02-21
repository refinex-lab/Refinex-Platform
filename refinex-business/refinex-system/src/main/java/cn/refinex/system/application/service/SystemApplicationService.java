package cn.refinex.system.application.service;

import cn.refinex.api.user.model.dto.UserManageDTO;
import cn.refinex.api.user.model.dto.UserManageListQuery;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.PageResponse;
import cn.refinex.base.utils.PageUtils;
import cn.refinex.base.utils.UniqueCodeUtils;
import cn.refinex.system.application.assembler.SystemDomainAssembler;
import cn.refinex.system.application.command.AssignRolePermissionsCommand;
import cn.refinex.system.application.command.AssignRoleUsersCommand;
import cn.refinex.system.application.command.CreateMenuCommand;
import cn.refinex.system.application.command.CreateMenuOpCommand;
import cn.refinex.system.application.command.CreateRoleCommand;
import cn.refinex.system.application.command.CreateSystemCommand;
import cn.refinex.system.application.command.QueryMenuTreeCommand;
import cn.refinex.system.application.command.QueryRoleListCommand;
import cn.refinex.system.application.command.QuerySystemListCommand;
import cn.refinex.system.application.command.UpdateMenuCommand;
import cn.refinex.system.application.command.UpdateMenuOpCommand;
import cn.refinex.system.application.command.UpdateRoleCommand;
import cn.refinex.system.application.command.UpdateSystemCommand;
import cn.refinex.system.application.dto.MenuDTO;
import cn.refinex.system.application.dto.MenuOpDTO;
import cn.refinex.system.application.dto.MenuOpManageDTO;
import cn.refinex.system.application.dto.MenuTreeNodeDTO;
import cn.refinex.system.application.dto.OpDTO;
import cn.refinex.system.application.dto.RoleBindingDTO;
import cn.refinex.system.application.dto.RoleBindingUserDTO;
import cn.refinex.system.application.dto.RoleDTO;
import cn.refinex.system.application.dto.SystemDTO;
import cn.refinex.system.domain.error.SystemErrorCode;
import cn.refinex.system.domain.model.entity.MenuEntity;
import cn.refinex.system.domain.model.entity.MenuOpEntity;
import cn.refinex.system.domain.model.entity.OpEntity;
import cn.refinex.system.domain.model.entity.RoleEntity;
import cn.refinex.system.domain.model.entity.SystemEntity;
import cn.refinex.system.domain.repository.SystemRepository;
import cn.refinex.system.infrastructure.client.user.UserManageRemoteGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

    private static final int MAX_CODE_GENERATE_ATTEMPTS = 10;

    private final SystemRepository systemRepository;
    private final SystemDomainAssembler systemDomainAssembler;
    private final UserManageRemoteGateway userManageRemoteGateway;

    /**
     * 查询系统列表
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
     */
    @Transactional(rollbackFor = Exception.class)
    public SystemDTO updateSystem(UpdateSystemCommand command) {
        if (command == null || command.getSystemId() == null || isBlank(command.getSystemName())) {
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
     */
    public PageResponse<RoleDTO> listRoles(QueryRoleListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);

        PageResponse<RoleEntity> entities = systemRepository.listRoles(
                command == null ? null : command.getEstabId(),
                command == null ? null : command.getStatus(),
                command == null ? null : command.getKeyword(),
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
     */
    public RoleDTO getRole(Long roleId) {
        RoleEntity role = requireRole(roleId);
        return systemDomainAssembler.toRoleDto(role);
    }

    /**
     * 创建角色
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleDTO createRole(CreateRoleCommand command) {
        if (command == null || isBlank(command.getRoleName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        Long estabId = defaultIfNull(command.getEstabId(), 0L);
        String roleCode = generateRoleCode(estabId, command.getRoleCode());

        RoleEntity entity = new RoleEntity();
        entity.setEstabId(estabId);
        entity.setRoleCode(roleCode);
        entity.setRoleName(command.getRoleName().trim());
        entity.setRoleType(defaultIfNull(command.getRoleType(), 2));
        entity.setIsBuiltin(defaultIfNull(command.getIsBuiltin(), 0));
        entity.setStatus(defaultIfNull(command.getStatus(), 1));
        entity.setSort(defaultIfNull(command.getSort(), 0));
        entity.setRemark(trimToNull(command.getRemark()));

        RoleEntity created = systemRepository.insertRole(entity);
        return systemDomainAssembler.toRoleDto(created);
    }

    /**
     * 更新角色
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleDTO updateRole(UpdateRoleCommand command) {
        if (command == null || command.getRoleId() == null || isBlank(command.getRoleName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        RoleEntity existing = requireRole(command.getRoleId());
        existing.setRoleName(command.getRoleName().trim());
        existing.setRoleType(defaultIfNull(command.getRoleType(), existing.getRoleType()));
        existing.setIsBuiltin(defaultIfNull(command.getIsBuiltin(), existing.getIsBuiltin()));
        existing.setStatus(defaultIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(defaultIfNull(command.getSort(), existing.getSort()));
        existing.setRemark(trimToNull(command.getRemark()));
        systemRepository.updateRole(existing);
        return systemDomainAssembler.toRoleDto(requireRole(existing.getId()));
    }

    /**
     * 查询角色授权信息
     */
    public RoleBindingDTO getRoleBindings(Long roleId) {
        requireRole(roleId);
        RoleBindingDTO dto = new RoleBindingDTO();
        List<Long> userIds = systemRepository.listRoleUserIds(roleId);
        dto.setUserIds(userIds);
        dto.setUsers(listRoleBindingUsers(userIds));
        dto.setMenuIds(systemRepository.listRoleMenuIds(roleId));
        dto.setMenuOpIds(systemRepository.listRoleMenuOpIds(roleId));
        dto.setDrsInterfaceIds(systemRepository.listRoleDrsInterfaceIds(roleId));
        return dto;
    }

    /**
     * 角色授权用户
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
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRolePermissions(AssignRolePermissionsCommand command) {
        if (command == null || command.getRoleId() == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        RoleEntity role = requireRole(command.getRoleId());

        List<Long> menuIds = normalizeIdList(command.getMenuIds());
        if (!menuIds.isEmpty()) {
            long existingMenuCount = systemRepository.countMenusByIdsAndEstabId(role.getEstabId(), menuIds);
            if (existingMenuCount != menuIds.size()) {
                throw new BizException(SystemErrorCode.MENU_NOT_FOUND);
            }
        }

        List<Long> menuOpIds = normalizeIdList(command.getMenuOpIds());
        if (!menuOpIds.isEmpty()) {
            long existingOpCount = systemRepository.countMenuOpsByIdsAndEstabId(role.getEstabId(), menuOpIds);
            if (existingOpCount != menuOpIds.size()) {
                throw new BizException(SystemErrorCode.MENU_OP_NOT_FOUND);
            }
        }

        List<Long> drsInterfaceIds = normalizeIdList(command.getDrsInterfaceIds());
        if (!drsInterfaceIds.isEmpty()) {
            long existingDrsInterfaceCount = systemRepository.countDrsInterfacesByIdsAndEstabId(
                    role.getEstabId(),
                    drsInterfaceIds
            );
            if (existingDrsInterfaceCount != drsInterfaceIds.size()) {
                throw new BizException(SystemErrorCode.DRS_INTERFACE_NOT_FOUND);
            }
        }

        systemRepository.replaceRoleMenus(role.getId(), menuIds, menuOpIds, command.getOperatorUserId());
        systemRepository.replaceRoleDrsInterfaces(role.getId(), drsInterfaceIds, command.getOperatorUserId());
    }

    /**
     * 查询菜单详情
     */
    public MenuDTO getMenu(Long menuId) {
        MenuEntity menu = requireMenu(menuId);
        return systemDomainAssembler.toMenuDto(menu);
    }

    /**
     * 创建菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public MenuDTO createMenu(CreateMenuCommand command) {
        if (command == null || isBlank(command.getMenuName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        Long parentId = defaultIfNull(command.getParentId(), 0L);
        Long estabId = command.getEstabId();
        Long systemId = command.getSystemId();

        // 从父菜单继承 estabId 和 systemId
        if (parentId > 0) {
            MenuEntity parent = requireMenu(parentId);
            if (estabId == null) {
                estabId = parent.getEstabId();
            }
            if (systemId == null) {
                systemId = parent.getSystemId();
            }
            if (!Objects.equals(parent.getSystemId(), systemId)
                    || !Objects.equals(parent.getEstabId(), estabId)) {
                throw new BizException(SystemErrorCode.INVALID_PARAM);
            }
        }

        estabId = defaultIfNull(estabId, 0L);
        if (systemId == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        requireSystem(systemId);

        String menuCode = generateMenuCode(estabId, systemId, command.getMenuCode());

        MenuEntity entity = new MenuEntity();
        entity.setEstabId(estabId);
        entity.setSystemId(systemId);
        entity.setParentId(parentId);
        entity.setMenuCode(menuCode);
        entity.setMenuName(command.getMenuName().trim());
        entity.setMenuType(defaultIfNull(command.getMenuType(), 1));
        entity.setPath(trimToNull(command.getPath()));
        entity.setIcon(trimToNull(command.getIcon()));
        entity.setIsBuiltin(defaultIfNull(command.getIsBuiltin(), 0));
        entity.setVisible(defaultIfNull(command.getVisible(), 1));
        entity.setIsFrame(defaultIfNull(command.getIsFrame(), 0));
        entity.setStatus(defaultIfNull(command.getStatus(), 1));
        entity.setSort(defaultIfNull(command.getSort(), 0));

        MenuEntity created = systemRepository.insertMenu(entity);
        return systemDomainAssembler.toMenuDto(created);
    }

    /**
     * 更新菜单
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
        Long systemId = defaultIfNull(command.getSystemId(), existing.getSystemId());
        if (Objects.equals(parentId, existing.getId())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        if (parentId > 0) {
            MenuEntity parent = requireMenu(parentId);
            if (!Objects.equals(parent.getEstabId(), existing.getEstabId())) {
                throw new BizException(SystemErrorCode.INVALID_PARAM);
            }
        }

        String menuCode = command.getMenuCode().trim();
        if (systemRepository.countMenuCode(existing.getEstabId(), systemId, menuCode, existing.getId()) > 0) {
            throw new BizException(SystemErrorCode.MENU_CODE_DUPLICATED);
        }

        existing.setParentId(parentId);
        existing.setSystemId(systemId);
        existing.setMenuCode(menuCode);
        existing.setMenuName(command.getMenuName().trim());
        existing.setMenuType(defaultIfNull(command.getMenuType(), existing.getMenuType()));
        existing.setPath(trimToNull(command.getPath()));
        existing.setIcon(trimToNull(command.getIcon()));
        existing.setIsBuiltin(defaultIfNull(command.getIsBuiltin(), existing.getIsBuiltin()));
        existing.setVisible(defaultIfNull(command.getVisible(), existing.getVisible()));
        existing.setIsFrame(defaultIfNull(command.getIsFrame(), existing.getIsFrame()));
        existing.setStatus(defaultIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(defaultIfNull(command.getSort(), existing.getSort()));

        systemRepository.updateMenu(existing);
        return systemDomainAssembler.toMenuDto(requireMenu(existing.getId()));
    }

    /**
     * 删除菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long menuId) {
        MenuEntity menu = requireMenu(menuId);
        if (menu.getIsBuiltin() != null && menu.getIsBuiltin() == 1) {
            throw new BizException(SystemErrorCode.MENU_BUILTIN_FORBIDDEN);
        }
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
     */
    public MenuOpManageDTO getMenuOp(Long menuOpId) {
        MenuOpEntity menuOp = requireMenuOp(menuOpId);
        return systemDomainAssembler.toMenuOpManageDto(menuOp);
    }

    /**
     * 创建菜单操作
     */
    @Transactional(rollbackFor = Exception.class)
    public MenuOpManageDTO createMenuOp(CreateMenuOpCommand command) {
        if (command == null || command.getMenuId() == null || isBlank(command.getOpName())) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        MenuEntity menu = requireMenu(command.getMenuId());
        String opCode = generateMenuOpCode(menu.getId(), command.getOpCode());

        MenuOpEntity entity = new MenuOpEntity();
        entity.setMenuId(menu.getId());
        entity.setOpCode(opCode);
        entity.setOpName(command.getOpName().trim());
        entity.setStatus(defaultIfNull(command.getStatus(), 1));
        entity.setSort(defaultIfNull(command.getSort(), 0));

        MenuOpEntity created = systemRepository.insertMenuOp(entity);
        return systemDomainAssembler.toMenuOpManageDto(created);
    }

    /**
     * 更新菜单操作
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
        existing.setStatus(defaultIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(defaultIfNull(command.getSort(), existing.getSort()));

        systemRepository.updateMenuOp(existing);
        return systemDomainAssembler.toMenuOpManageDto(requireMenuOp(existing.getId()));
    }

    /**
     * 删除菜单操作
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenuOp(Long menuOpId) {
        MenuOpEntity menuOp = requireMenuOp(menuOpId);
        systemRepository.deleteRoleMenuOpBindingsByMenuOpId(menuOp.getId());
        systemRepository.deleteMenuOpById(menuOp.getId());
    }

    /**
     * 查询启用的操作定义列表
     */
    public List<OpDTO> listOps() {
        List<OpEntity> entities = systemRepository.listOps();
        List<OpDTO> result = new ArrayList<>();
        for (OpEntity entity : entities) {
            result.add(systemDomainAssembler.toOpDto(entity));
        }
        return result;
    }

    /**
     * 按系统查询菜单树（兼容旧调用）
     */
    public List<MenuTreeNodeDTO> getMenuTree(QueryMenuTreeCommand command) {
        if (command == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        Long estabId = defaultIfNull(command.getEstabId(), 0L);
        Set<Long> assignedMenuIds = new LinkedHashSet<>();
        Set<Long> assignedMenuOpIds = new LinkedHashSet<>();
        if (command.getRoleId() != null) {
            RoleEntity role = requireRole(command.getRoleId());
            if (!Objects.equals(role.getEstabId(), estabId)) {
                throw new BizException(SystemErrorCode.INVALID_PARAM);
            }
            assignedMenuIds.addAll(systemRepository.listRoleMenuIds(role.getId()));
            assignedMenuOpIds.addAll(systemRepository.listRoleMenuOpIds(role.getId()));
        }

        List<MenuEntity> menuEntities;
        List<MenuOpEntity> menuOpEntities;
        if (command.getSystemId() != null) {
            requireSystem(command.getSystemId());
            menuEntities = systemRepository.listMenus(estabId, command.getSystemId());
            menuOpEntities = systemRepository.listMenuOps(estabId, command.getSystemId());
        } else {
            // 查询当前企业 + 平台内置菜单
            List<Long> estabIds = new ArrayList<>();
            estabIds.add(0L);
            if (estabId > 0) {
                estabIds.add(estabId);
            }
            menuEntities = systemRepository.listMenusByEstabIds(estabIds);
            menuOpEntities = systemRepository.listMenuOpsByEstabIds(estabIds);
        }

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

    private List<RoleBindingUserDTO> listRoleBindingUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }

        UserManageListQuery query = new UserManageListQuery();
        query.setUserIds(userIds);
        query.setCurrentPage(1);
        query.setPageSize(Math.max(userIds.size(), 1));
        PageResponse<UserManageDTO> pageResponse = userManageRemoteGateway.listUsers(query);
        List<UserManageDTO> users = pageResponse.getData() == null ? new ArrayList<>() : pageResponse.getData();
        Map<Long, UserManageDTO> userMap = new LinkedHashMap<>();
        for (UserManageDTO user : users) {
            if (user != null && user.getUserId() != null) {
                userMap.put(user.getUserId(), user);
            }
        }

        List<RoleBindingUserDTO> result = new ArrayList<>();
        for (Long userId : userIds) {
            UserManageDTO user = userMap.get(userId);
            if (user == null) {
                continue;
            }
            RoleBindingUserDTO dto = new RoleBindingUserDTO();
            dto.setUserId(user.getUserId());
            dto.setUserCode(user.getUserCode());
            dto.setUsername(user.getUsername());
            dto.setDisplayName(user.getDisplayName());
            result.add(dto);
        }
        return result;
    }

    private String generateRoleCode(Long estabId, String code) {
        if (!isBlank(code)) {
            String normalized = code.trim();
            if (systemRepository.countRoleCode(estabId, normalized, null) > 0) {
                throw new BizException(SystemErrorCode.ROLE_CODE_DUPLICATED);
            }
            return normalized;
        }
        for (int i = 0; i < MAX_CODE_GENERATE_ATTEMPTS; i++) {
            String candidate = UniqueCodeUtils.randomUpperCode("ROLE_", 8);
            if (systemRepository.countRoleCode(estabId, candidate, null) == 0) {
                return candidate;
            }
        }
        throw new BizException("自动生成角色编码失败，请稍后重试", SystemErrorCode.ROLE_CODE_DUPLICATED);
    }

    private String generateMenuCode(Long estabId, Long systemId, String code) {
        if (!isBlank(code)) {
            String normalized = code.trim();
            if (systemRepository.countMenuCode(estabId, systemId, normalized, null) > 0) {
                throw new BizException(SystemErrorCode.MENU_CODE_DUPLICATED);
            }
            return normalized;
        }
        for (int i = 0; i < MAX_CODE_GENERATE_ATTEMPTS; i++) {
            String candidate = UniqueCodeUtils.randomUpperCode("MENU_", 8);
            if (systemRepository.countMenuCode(estabId, systemId, candidate, null) == 0) {
                return candidate;
            }
        }
        throw new BizException("自动生成菜单编码失败，请稍后重试", SystemErrorCode.MENU_CODE_DUPLICATED);
    }

    private String generateMenuOpCode(Long menuId, String code) {
        if (!isBlank(code)) {
            String normalized = code.trim();
            if (systemRepository.countMenuOpCode(menuId, normalized, null) > 0) {
                throw new BizException(SystemErrorCode.MENU_OP_CODE_DUPLICATED);
            }
            return normalized;
        }
        for (int i = 0; i < MAX_CODE_GENERATE_ATTEMPTS; i++) {
            String candidate = UniqueCodeUtils.randomUpperCode("OP_", 8);
            if (systemRepository.countMenuOpCode(menuId, candidate, null) == 0) {
                return candidate;
            }
        }
        throw new BizException("自动生成菜单操作编码失败，请稍后重试", SystemErrorCode.MENU_OP_CODE_DUPLICATED);
    }

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
