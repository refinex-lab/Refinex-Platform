package cn.refinex.system.domain.repository;

import cn.refinex.base.response.PageResponse;
import cn.refinex.system.domain.model.entity.MenuEntity;
import cn.refinex.system.domain.model.entity.MenuOpEntity;
import cn.refinex.system.domain.model.entity.RoleEntity;
import cn.refinex.system.domain.model.entity.SystemEntity;

import java.util.List;

/**
 * 系统管理仓储
 *
 * @author refinex
 */
public interface SystemRepository {

    /**
     * 查询系统列表
     */
    PageResponse<SystemEntity> listSystems(Integer status, String keyword, int currentPage, int pageSize);

    /**
     * 根据系统ID查询系统
     */
    SystemEntity findSystemById(Long systemId);

    /**
     * 根据系统编码统计系统数量
     */
    long countSystemCode(String systemCode, Long excludeSystemId);

    /**
     * 插入系统
     */
    SystemEntity insertSystem(SystemEntity system);

    /**
     * 更新系统
     */
    void updateSystem(SystemEntity system);

    /**
     * 查询角色列表
     */
    PageResponse<RoleEntity> listRoles(Long estabId, Integer status, String keyword, int currentPage, int pageSize);

    /**
     * 查询角色
     */
    RoleEntity findRoleById(Long roleId);

    /**
     * 统计角色编码
     */
    long countRoleCode(Long estabId, String roleCode, Long excludeRoleId);

    /**
     * 插入角色
     */
    RoleEntity insertRole(RoleEntity role);

    /**
     * 更新角色
     */
    void updateRole(RoleEntity role);

    /**
     * 查询角色用户ID列表
     */
    List<Long> listRoleUserIds(Long roleId);

    /**
     * 替换角色用户
     */
    void replaceRoleUsers(Long roleId, Long estabId, List<Long> userIds, Long operatorUserId);

    /**
     * 查询角色菜单ID列表
     */
    List<Long> listRoleMenuIds(Long roleId);

    /**
     * 查询角色菜单操作ID列表
     */
    List<Long> listRoleMenuOpIds(Long roleId);

    /**
     * 查询角色数据资源接口ID列表
     */
    List<Long> listRoleDrsInterfaceIds(Long roleId);

    /**
     * 替换角色菜单与操作
     */
    void replaceRoleMenus(Long roleId, List<Long> menuIds, List<Long> menuOpIds, Long operatorUserId);

    /**
     * 替换角色数据资源接口授权
     */
    void replaceRoleDrsInterfaces(Long roleId, List<Long> drsInterfaceIds, Long operatorUserId);

    /**
     * 按企业与系统查询菜单
     */
    List<MenuEntity> listMenus(Long estabId, Long systemId);

    /**
     * 查询菜单
     */
    MenuEntity findMenuById(Long menuId);

    /**
     * 统计菜单编码
     */
    long countMenuCode(Long estabId, Long systemId, String menuCode, Long excludeMenuId);

    /**
     * 插入菜单
     */
    MenuEntity insertMenu(MenuEntity menu);

    /**
     * 更新菜单
     */
    void updateMenu(MenuEntity menu);

    /**
     * 删除菜单
     */
    void deleteMenuById(Long menuId);

    /**
     * 统计子菜单数量
     */
    long countChildMenus(Long menuId);

    /**
     * 统计菜单操作数量
     */
    long countMenuOpsByMenuId(Long menuId);

    /**
     * 删除菜单绑定
     */
    void deleteRoleMenuBindingsByMenuId(Long menuId);

    /**
     * 查询菜单操作列表
     */
    PageResponse<MenuOpEntity> listMenuOpsByMenuId(Long menuId, int currentPage, int pageSize);

    /**
     * 按企业与系统查询菜单操作列表
     */
    List<MenuOpEntity> listMenuOps(Long estabId, Long systemId);

    /**
     * 查询菜单操作
     */
    MenuOpEntity findMenuOpById(Long menuOpId);

    /**
     * 统计菜单操作编码
     */
    long countMenuOpCode(Long menuId, String opCode, Long excludeMenuOpId);

    /**
     * 插入菜单操作
     */
    MenuOpEntity insertMenuOp(MenuOpEntity menuOp);

    /**
     * 更新菜单操作
     */
    void updateMenuOp(MenuOpEntity menuOp);

    /**
     * 删除菜单操作
     */
    void deleteMenuOpById(Long menuOpId);

    /**
     * 删除菜单操作绑定
     */
    void deleteRoleMenuOpBindingsByMenuOpId(Long menuOpId);

    /**
     * 按企业统计菜单ID数量
     */
    long countMenusByIdsAndEstabId(Long estabId, List<Long> menuIds);

    /**
     * 按企业统计菜单操作ID数量
     */
    long countMenuOpsByIdsAndEstabId(Long estabId, List<Long> menuOpIds);

    /**
     * 按企业统计数据资源接口ID数量
     */
    long countDrsInterfacesByIdsAndEstabId(Long estabId, List<Long> drsInterfaceIds);
}
