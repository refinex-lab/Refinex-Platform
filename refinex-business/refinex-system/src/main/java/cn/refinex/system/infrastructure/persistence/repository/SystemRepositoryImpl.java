package cn.refinex.system.infrastructure.persistence.repository;

import cn.refinex.base.response.PageResponse;
import cn.refinex.system.domain.model.entity.MenuEntity;
import cn.refinex.system.domain.model.entity.MenuOpEntity;
import cn.refinex.system.domain.model.entity.RoleEntity;
import cn.refinex.system.domain.model.entity.SystemEntity;
import cn.refinex.system.domain.repository.SystemRepository;
import cn.refinex.system.infrastructure.converter.MenuDoConverter;
import cn.refinex.system.infrastructure.converter.MenuOpDoConverter;
import cn.refinex.system.infrastructure.converter.RoleDoConverter;
import cn.refinex.system.infrastructure.converter.SystemDoConverter;
import cn.refinex.system.infrastructure.persistence.dataobject.*;
import cn.refinex.system.infrastructure.persistence.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统管理仓储实现
 *
 * @author refinex
 */
@Repository
@RequiredArgsConstructor
public class SystemRepositoryImpl implements SystemRepository {

    private final ScrSystemMapper scrSystemMapper;
    private final ScrRoleMapper scrRoleMapper;
    private final ScrRoleUserMapper scrRoleUserMapper;
    private final ScrMenuMapper scrMenuMapper;
    private final ScrMenuOpMapper scrMenuOpMapper;
    private final ScrRoleMenuMapper scrRoleMenuMapper;
    private final ScrRoleMenuOpMapper scrRoleMenuOpMapper;
    private final ScrRoleDrsInterfaceMapper scrRoleDrsInterfaceMapper;
    private final ScrDrsInterfaceMapper scrDrsInterfaceMapper;
    private final SystemDoConverter systemDoConverter;
    private final RoleDoConverter roleDoConverter;
    private final MenuDoConverter menuDoConverter;
    private final MenuOpDoConverter menuOpDoConverter;

    /**
     * 查询系统列表
     *
     * @param status  状态
     * @param keyword 关键字
     * @return 系统列表
     */
    @Override
    public PageResponse<SystemEntity> listSystems(Integer status, String keyword, int currentPage, int pageSize) {
        LambdaQueryWrapper<ScrSystemDo> query = Wrappers.lambdaQuery(ScrSystemDo.class)
                .eq(ScrSystemDo::getDeleted, 0)
                .orderByAsc(ScrSystemDo::getSort, ScrSystemDo::getId);
        if (status != null) {
            query.eq(ScrSystemDo::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(w -> w.like(ScrSystemDo::getSystemCode, trimmed).or().like(ScrSystemDo::getSystemName, trimmed));
        }

        Page<ScrSystemDo> page = new Page<>(currentPage, pageSize);
        Page<ScrSystemDo> rowsPage = scrSystemMapper.selectPage(page, query);
        List<ScrSystemDo> rows = rowsPage.getRecords();
        List<SystemEntity> result = new ArrayList<>();
        for (ScrSystemDo row : rows) {
            result.add(systemDoConverter.toEntity(row));
        }
        return PageResponse.of(result, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 根据系统ID查询系统
     *
     * @param systemId 系统ID
     * @return 系统
     */
    @Override
    public SystemEntity findSystemById(Long systemId) {
        ScrSystemDo row = scrSystemMapper.selectById(systemId);
        return row == null ? null : systemDoConverter.toEntity(row);
    }

    /**
     * 根据系统编号统计系统数量
     *
     * @param systemCode      系统编号
     * @param excludeSystemId 排除的系统ID
     * @return 系统数量
     */
    @Override
    public long countSystemCode(String systemCode, Long excludeSystemId) {
        LambdaQueryWrapper<ScrSystemDo> query = Wrappers.lambdaQuery(ScrSystemDo.class)
                .eq(ScrSystemDo::getSystemCode, systemCode)
                .eq(ScrSystemDo::getDeleted, 0);
        if (excludeSystemId != null) {
            query.ne(ScrSystemDo::getId, excludeSystemId);
        }
        Long count = scrSystemMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 新增系统
     *
     * @param system 系统
     * @return 新增后的系统
     */
    @Override
    public SystemEntity insertSystem(SystemEntity system) {
        ScrSystemDo row = systemDoConverter.toDo(system);
        scrSystemMapper.insert(row);
        return systemDoConverter.toEntity(row);
    }

    /**
     * 修改系统
     *
     * @param system 系统
     */
    @Override
    public void updateSystem(SystemEntity system) {
        ScrSystemDo row = systemDoConverter.toDo(system);
        scrSystemMapper.updateById(row);
    }

    /**
     * 查询系统角色列表
     *
     * @param systemId 系统ID
     * @param estabId  企业ID
     * @param status   状态
     * @param keyword  关键字
     * @return 角色列表
     */
    @Override
    public PageResponse<RoleEntity> listRoles(Long systemId, Long estabId, Integer status, String keyword,
                                              int currentPage, int pageSize) {
        LambdaQueryWrapper<ScrRoleDo> query = Wrappers.lambdaQuery(ScrRoleDo.class)
                .eq(ScrRoleDo::getDeleted, 0)
                .eq(ScrRoleDo::getSystemId, systemId)
                .orderByAsc(ScrRoleDo::getSort, ScrRoleDo::getId);
        if (estabId != null) {
            query.eq(ScrRoleDo::getEstabId, estabId);
        }
        if (status != null) {
            query.eq(ScrRoleDo::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(w -> w.like(ScrRoleDo::getRoleCode, trimmed).or().like(ScrRoleDo::getRoleName, trimmed));
        }

        Page<ScrRoleDo> page = new Page<>(currentPage, pageSize);
        Page<ScrRoleDo> rowsPage = scrRoleMapper.selectPage(page, query);
        List<ScrRoleDo> rows = rowsPage.getRecords();
        List<RoleEntity> result = new ArrayList<>();
        for (ScrRoleDo row : rows) {
            result.add(roleDoConverter.toEntity(row));
        }
        return PageResponse.of(result, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 根据角色ID查询角色
     *
     * @param roleId 角色ID
     * @return 角色
     */
    @Override
    public RoleEntity findRoleById(Long roleId) {
        ScrRoleDo row = scrRoleMapper.selectById(roleId);
        return row == null ? null : roleDoConverter.toEntity(row);
    }

    /**
     * 根据系统ID、企业ID和角色编号统计角色数量
     *
     * @param systemId      系统ID
     * @param estabId       企业ID
     * @param roleCode      角色编号
     * @param excludeRoleId 排除的角色ID
     * @return 角色数量
     */
    @Override
    public long countRoleCode(Long systemId, Long estabId, String roleCode, Long excludeRoleId) {
        LambdaQueryWrapper<ScrRoleDo> query = Wrappers.lambdaQuery(ScrRoleDo.class)
                .eq(ScrRoleDo::getSystemId, systemId)
                .eq(ScrRoleDo::getEstabId, estabId)
                .eq(ScrRoleDo::getRoleCode, roleCode)
                .eq(ScrRoleDo::getDeleted, 0);
        if (excludeRoleId != null) {
            query.ne(ScrRoleDo::getId, excludeRoleId);
        }
        Long count = scrRoleMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 新增角色
     *
     * @param role 角色
     * @return 新增后的角色
     */
    @Override
    public RoleEntity insertRole(RoleEntity role) {
        ScrRoleDo row = roleDoConverter.toDo(role);
        scrRoleMapper.insert(row);
        return roleDoConverter.toEntity(row);
    }

    /**
     * 修改角色
     *
     * @param role 角色
     */
    @Override
    public void updateRole(RoleEntity role) {
        ScrRoleDo row = roleDoConverter.toDo(role);
        scrRoleMapper.updateById(row);
    }

    /**
     * 根据角色ID查询用户ID列表
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    @Override
    public List<Long> listRoleUserIds(Long roleId) {
        return scrRoleUserMapper.selectUserIdsByRoleId(roleId);
    }

    /**
     * 替换角色用户
     *
     * @param roleId         角色ID
     * @param estabId        企业ID
     * @param userIds        用户ID列表
     * @param operatorUserId 操作员用户ID
     */
    @Override
    public void replaceRoleUsers(Long roleId, Long estabId, List<Long> userIds, Long operatorUserId) {
        scrRoleUserMapper.deleteByRoleIdHard(roleId);
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Long userId : userIds) {
            ScrRoleUserDo row = new ScrRoleUserDo();
            row.setRoleId(roleId);
            row.setUserId(userId);
            row.setEstabId(estabId);
            row.setGrantedBy(operatorUserId);
            row.setGrantedTime(now);
            row.setStatus(1);
            scrRoleUserMapper.insert(row);
        }
    }

    /**
     * 查询角色菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    @Override
    public List<Long> listRoleMenuIds(Long roleId) {
        return scrRoleMenuMapper.selectMenuIdsByRoleId(roleId);
    }

    /**
     * 查询角色菜单操作ID列表
     *
     * @param roleId 角色ID
     * @return 菜单操作ID列表
     */
    @Override
    public List<Long> listRoleMenuOpIds(Long roleId) {
        return scrRoleMenuOpMapper.selectMenuOpIdsByRoleId(roleId);
    }

    /**
     * 查询角色数据资源接口ID列表
     *
     * @param roleId 角色ID
     * @return 数据资源接口ID列表
     */
    @Override
    public List<Long> listRoleDrsInterfaceIds(Long roleId) {
        return scrRoleDrsInterfaceMapper.selectDrsInterfaceIdsByRoleId(roleId);
    }

    /**
     * 替换角色菜单
     *
     * @param roleId         角色ID
     * @param menuIds        菜单ID列表
     * @param menuOpIds      菜单操作ID列表
     * @param operatorUserId 操作员用户ID
     */
    @Override
    public void replaceRoleMenus(Long roleId, List<Long> menuIds, List<Long> menuOpIds, Long operatorUserId) {
        scrRoleMenuMapper.deleteByRoleIdHard(roleId);
        scrRoleMenuOpMapper.deleteByRoleIdHard(roleId);

        LocalDateTime now = LocalDateTime.now();
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                ScrRoleMenuDo row = new ScrRoleMenuDo();
                row.setRoleId(roleId);
                row.setMenuId(menuId);
                row.setGrantedBy(operatorUserId);
                row.setGrantedTime(now);
                scrRoleMenuMapper.insert(row);
            }
        }
        if (menuOpIds != null && !menuOpIds.isEmpty()) {
            for (Long menuOpId : menuOpIds) {
                ScrRoleMenuOpDo row = new ScrRoleMenuOpDo();
                row.setRoleId(roleId);
                row.setMenuOpId(menuOpId);
                row.setGrantedBy(operatorUserId);
                row.setGrantedTime(now);
                scrRoleMenuOpMapper.insert(row);
            }
        }
    }

    /**
     * 替换角色数据资源接口授权
     *
     * @param roleId           角色ID
     * @param drsInterfaceIds  数据资源接口ID列表
     * @param operatorUserId   操作员用户ID
     */
    @Override
    public void replaceRoleDrsInterfaces(Long roleId, List<Long> drsInterfaceIds, Long operatorUserId) {
        scrRoleDrsInterfaceMapper.deleteByRoleIdHard(roleId);
        if (drsInterfaceIds == null || drsInterfaceIds.isEmpty()) {
            return;
        }
        for (Long drsInterfaceId : drsInterfaceIds) {
            ScrRoleDrsInterfaceDo row = new ScrRoleDrsInterfaceDo();
            row.setRoleId(roleId);
            row.setDrsInterfaceId(drsInterfaceId);
            row.setCreateBy(operatorUserId);
            row.setUpdateBy(operatorUserId);
            scrRoleDrsInterfaceMapper.insert(row);
        }
    }

    /**
     * 根据系统ID查询菜单列表
     *
     * @param systemId 系统ID
     * @return 菜单列表
     */
    @Override
    public List<MenuEntity> listMenusBySystemId(Long systemId) {
        List<ScrMenuDo> rows = scrMenuMapper.selectList(
                Wrappers.lambdaQuery(ScrMenuDo.class)
                        .eq(ScrMenuDo::getSystemId, systemId)
                        .eq(ScrMenuDo::getDeleted, 0)
                        .orderByAsc(ScrMenuDo::getSort, ScrMenuDo::getId)
        );
        List<MenuEntity> result = new ArrayList<>();
        for (ScrMenuDo row : rows) {
            result.add(menuDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 根据菜单ID查询菜单
     *
     * @param menuId 菜单ID
     * @return 菜单
     */
    @Override
    public MenuEntity findMenuById(Long menuId) {
        ScrMenuDo row = scrMenuMapper.selectById(menuId);
        return row == null ? null : menuDoConverter.toEntity(row);
    }

    /**
     * 根据系统ID和菜单编码统计菜单数量
     *
     * @param systemId      系统ID
     * @param menuCode      菜单编码
     * @param excludeMenuId 排除菜单ID
     * @return 菜单数量
     */
    @Override
    public long countMenuCode(Long systemId, String menuCode, Long excludeMenuId) {
        LambdaQueryWrapper<ScrMenuDo> query = Wrappers.lambdaQuery(ScrMenuDo.class)
                .eq(ScrMenuDo::getSystemId, systemId)
                .eq(ScrMenuDo::getMenuCode, menuCode)
                .eq(ScrMenuDo::getDeleted, 0);
        if (excludeMenuId != null) {
            query.ne(ScrMenuDo::getId, excludeMenuId);
        }
        Long count = scrMenuMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 插入菜单
     *
     * @param menu 菜单
     * @return 菜单
     */
    @Override
    public MenuEntity insertMenu(MenuEntity menu) {
        ScrMenuDo row = new ScrMenuDo();
        row.setSystemId(menu.getSystemId());
        row.setParentId(menu.getParentId());
        row.setMenuCode(menu.getMenuCode());
        row.setMenuName(menu.getMenuName());
        row.setMenuType(menu.getMenuType());
        row.setPath(menu.getPath());
        row.setComponent(menu.getComponent());
        row.setPermissionKey(menu.getPermissionKey());
        row.setIcon(menu.getIcon());
        row.setVisible(menu.getVisible());
        row.setIsFrame(menu.getIsFrame());
        row.setIsCache(menu.getIsCache());
        row.setStatus(menu.getStatus());
        row.setSort(menu.getSort());
        scrMenuMapper.insert(row);
        return menuDoConverter.toEntity(row);
    }

    /**
     * 更新菜单
     *
     * @param menu 菜单
     */
    @Override
    public void updateMenu(MenuEntity menu) {
        ScrMenuDo row = new ScrMenuDo();
        row.setId(menu.getId());
        row.setParentId(menu.getParentId());
        row.setMenuCode(menu.getMenuCode());
        row.setMenuName(menu.getMenuName());
        row.setMenuType(menu.getMenuType());
        row.setPath(menu.getPath());
        row.setComponent(menu.getComponent());
        row.setPermissionKey(menu.getPermissionKey());
        row.setIcon(menu.getIcon());
        row.setVisible(menu.getVisible());
        row.setIsFrame(menu.getIsFrame());
        row.setIsCache(menu.getIsCache());
        row.setStatus(menu.getStatus());
        row.setSort(menu.getSort());
        scrMenuMapper.updateById(row);
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     */
    @Override
    public void deleteMenuById(Long menuId) {
        scrMenuMapper.deleteById(menuId);
    }

    /**
     * 统计子菜单数量
     *
     * @param menuId 菜单ID
     * @return 子菜单数量
     */
    @Override
    public long countChildMenus(Long menuId) {
        Long count = scrMenuMapper.selectCount(
                Wrappers.lambdaQuery(ScrMenuDo.class)
                        .eq(ScrMenuDo::getParentId, menuId)
                        .eq(ScrMenuDo::getDeleted, 0)
        );
        return count == null ? 0L : count;
    }

    /**
     * 根据菜单ID统计操作数量
     *
     * @param menuId 菜单ID
     * @return 操作数量
     */
    @Override
    public long countMenuOpsByMenuId(Long menuId) {
        Long count = scrMenuOpMapper.selectCount(
                Wrappers.lambdaQuery(ScrMenuOpDo.class)
                        .eq(ScrMenuOpDo::getMenuId, menuId)
                        .eq(ScrMenuOpDo::getDeleted, 0)
        );
        return count == null ? 0L : count;
    }

    /**
     * 删除菜单绑定的角色菜单关系
     *
     * @param menuId 菜单ID
     */
    @Override
    public void deleteRoleMenuBindingsByMenuId(Long menuId) {
        scrRoleMenuMapper.deleteByMenuIdHard(menuId);
    }

    /**
     * 根据菜单ID查询菜单操作列表
     *
     * @param menuId 菜单ID
     * @return 菜单操作列表
     */
    @Override
    public PageResponse<MenuOpEntity> listMenuOpsByMenuId(Long menuId, int currentPage, int pageSize) {
        LambdaQueryWrapper<ScrMenuOpDo> query = Wrappers.lambdaQuery(ScrMenuOpDo.class)
                .eq(ScrMenuOpDo::getMenuId, menuId)
                .eq(ScrMenuOpDo::getDeleted, 0)
                .orderByAsc(ScrMenuOpDo::getSort, ScrMenuOpDo::getId);
        Page<ScrMenuOpDo> page = new Page<>(currentPage, pageSize);
        Page<ScrMenuOpDo> rowsPage = scrMenuOpMapper.selectPage(page, query);
        List<ScrMenuOpDo> rows = rowsPage.getRecords();
        List<MenuOpEntity> result = new ArrayList<>();
        for (ScrMenuOpDo row : rows) {
            result.add(menuOpDoConverter.toEntity(row));
        }
        return PageResponse.of(result, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 根据系统ID查询菜单操作列表
     *
     * @param systemId 系统ID
     * @return 菜单操作列表
     */
    @Override
    public List<MenuOpEntity> listMenuOpsBySystemId(Long systemId) {
        List<ScrMenuOpDo> rows = scrMenuOpMapper.selectBySystemId(systemId);
        List<MenuOpEntity> result = new ArrayList<>();
        for (ScrMenuOpDo row : rows) {
            result.add(menuOpDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 根据菜单操作ID查询菜单操作
     *
     * @param menuOpId 菜单操作ID
     * @return 菜单操作
     */
    @Override
    public MenuOpEntity findMenuOpById(Long menuOpId) {
        ScrMenuOpDo row = scrMenuOpMapper.selectById(menuOpId);
        return row == null ? null : menuOpDoConverter.toEntity(row);
    }

    /**
     * 根据菜单ID和操作编码统计菜单操作数量
     *
     * @param menuId          菜单ID
     * @param opCode          操作编码
     * @param excludeMenuOpId 排除菜单操作ID
     * @return 菜单操作数量
     */
    @Override
    public long countMenuOpCode(Long menuId, String opCode, Long excludeMenuOpId) {
        LambdaQueryWrapper<ScrMenuOpDo> query = Wrappers.lambdaQuery(ScrMenuOpDo.class)
                .eq(ScrMenuOpDo::getMenuId, menuId)
                .eq(ScrMenuOpDo::getOpCode, opCode)
                .eq(ScrMenuOpDo::getDeleted, 0);
        if (excludeMenuOpId != null) {
            query.ne(ScrMenuOpDo::getId, excludeMenuOpId);
        }
        Long count = scrMenuOpMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 插入菜单操作
     *
     * @param menuOp 菜单操作
     * @return 菜单操作
     */
    @Override
    public MenuOpEntity insertMenuOp(MenuOpEntity menuOp) {
        ScrMenuOpDo row = new ScrMenuOpDo();
        row.setMenuId(menuOp.getMenuId());
        row.setOpCode(menuOp.getOpCode());
        row.setOpName(menuOp.getOpName());
        row.setHttpMethod(menuOp.getHttpMethod());
        row.setPathPattern(menuOp.getPathPattern());
        row.setPermissionKey(menuOp.getPermissionKey());
        row.setStatus(menuOp.getStatus());
        row.setSort(menuOp.getSort());
        scrMenuOpMapper.insert(row);
        return menuOpDoConverter.toEntity(row);
    }

    /**
     * 更新菜单操作
     *
     * @param menuOp 菜单操作
     */
    @Override
    public void updateMenuOp(MenuOpEntity menuOp) {
        ScrMenuOpDo row = new ScrMenuOpDo();
        row.setId(menuOp.getId());
        row.setOpCode(menuOp.getOpCode());
        row.setOpName(menuOp.getOpName());
        row.setHttpMethod(menuOp.getHttpMethod());
        row.setPathPattern(menuOp.getPathPattern());
        row.setPermissionKey(menuOp.getPermissionKey());
        row.setStatus(menuOp.getStatus());
        row.setSort(menuOp.getSort());
        scrMenuOpMapper.updateById(row);
    }

    /**
     * 删除菜单操作
     *
     * @param menuOpId 菜单操作ID
     */
    @Override
    public void deleteMenuOpById(Long menuOpId) {
        scrMenuOpMapper.deleteById(menuOpId);
    }

    /**
     * 删除菜单操作绑定的角色关系
     *
     * @param menuOpId 菜单操作ID
     */
    @Override
    public void deleteRoleMenuOpBindingsByMenuOpId(Long menuOpId) {
        scrRoleMenuOpMapper.deleteByMenuOpIdHard(menuOpId);
    }

    /**
     * 根据系统ID和菜单ID列表统计菜单数量
     *
     * @param systemId 系统ID
     * @param menuIds  菜单ID列表
     * @return 菜单数量
     */
    @Override
    public long countMenusByIdsAndSystemId(Long systemId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return 0;
        }
        Long count = scrMenuMapper.selectCount(
                Wrappers.lambdaQuery(ScrMenuDo.class)
                        .eq(ScrMenuDo::getSystemId, systemId)
                        .eq(ScrMenuDo::getDeleted, 0)
                        .in(ScrMenuDo::getId, menuIds)
        );
        return count == null ? 0L : count;
    }

    /**
     * 根据系统ID和菜单操作ID列表统计菜单操作数量
     *
     * @param systemId  系统ID
     * @param menuOpIds 菜单操作ID列表
     * @return 菜单操作数量
     */
    @Override
    public long countMenuOpsByIdsAndSystemId(Long systemId, List<Long> menuOpIds) {
        if (menuOpIds == null || menuOpIds.isEmpty()) {
            return 0;
        }
        return scrMenuOpMapper.countByIdsAndSystemId(systemId, menuOpIds);
    }

    /**
     * 根据系统ID和数据资源接口ID列表统计数据资源接口数量
     *
     * @param systemId         系统ID
     * @param drsInterfaceIds  数据资源接口ID列表
     * @return 数据资源接口数量
     */
    @Override
    public long countDrsInterfacesByIdsAndSystemId(Long systemId, List<Long> drsInterfaceIds) {
        if (drsInterfaceIds == null || drsInterfaceIds.isEmpty()) {
            return 0;
        }
        return scrDrsInterfaceMapper.countByIdsAndSystemId(systemId, drsInterfaceIds);
    }
}
