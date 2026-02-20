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
     *
     * @param status  状态
     * @param keyword 关键字
     * @return 系统列表
     */
    PageResponse<SystemEntity> listSystems(Integer status, String keyword, int currentPage, int pageSize);

    /**
     * 根据系统ID查询系统
     *
     * @param systemId 系统ID
     * @return 系统
     */
    SystemEntity findSystemById(Long systemId);

    /**
     * 根据系统编码统计系统数量
     *
     * @param systemCode      系统编码
     * @param excludeSystemId 排除的系统ID
     * @return 系统数量
     */
    long countSystemCode(String systemCode, Long excludeSystemId);

    /**
     * 插入系统
     *
     * @param system 系统
     * @return 系统
     */
    SystemEntity insertSystem(SystemEntity system);

    /**
     * 更新系统
     *
     * @param system 系统
     */
    void updateSystem(SystemEntity system);

    /**
     * 查询系统角色列表
     *
     * @param systemId 系统ID
     * @param estabId  租户ID
     * @param status   状态
     * @param keyword  关键字
     * @return 角色列表
     */
    PageResponse<RoleEntity> listRoles(Long systemId, Long estabId, Integer status, String keyword, int currentPage, int pageSize);

    /**
     * 根据角色ID查询角色
     *
     * @param roleId 角色ID
     * @return 角色
     */
    RoleEntity findRoleById(Long roleId);

    /**
     * 根据系统ID、租户ID、角色编码统计角色数量
     *
     * @param systemId      系统ID
     * @param estabId       租户ID
     * @param roleCode      角色编码
     * @param excludeRoleId 排除的角色ID
     * @return 角色数量
     */
    long countRoleCode(Long systemId, Long estabId, String roleCode, Long excludeRoleId);

    /**
     * 插入角色
     *
     * @param role 角色
     * @return 角色
     */
    RoleEntity insertRole(RoleEntity role);

    /**
     * 更新角色
     *
     * @param role 角色
     */
    void updateRole(RoleEntity role);

    /**
     * 根据角色ID查询角色用户ID列表
     *
     * @param roleId 角色ID
     * @return 角色用户ID列表
     */
    List<Long> listRoleUserIds(Long roleId);

    /**
     * 替换角色用户
     *
     * @param roleId         角色ID
     * @param estabId        租户ID
     * @param userIds        用户ID列表
     * @param operatorUserId 操作员用户ID
     */
    void replaceRoleUsers(Long roleId, Long estabId, List<Long> userIds, Long operatorUserId);

    /**
     * 根据角色ID查询角色菜单ID列表
     *
     * @param roleId 角色ID
     * @return 角色菜单ID列表
     */
    List<Long> listRoleMenuIds(Long roleId);

    /**
     * 根据角色ID查询角色菜单操作ID列表
     *
     * @param roleId 角色ID
     * @return 角色菜单操作ID列表
     */
    List<Long> listRoleMenuOpIds(Long roleId);

    /**
     * 根据角色ID查询角色数据资源接口ID列表
     *
     * @param roleId 角色ID
     * @return 角色数据资源接口ID列表
     */
    List<Long> listRoleDrsInterfaceIds(Long roleId);

    /**
     * 替换角色菜单与操作
     *
     * @param roleId         角色ID
     * @param menuIds        菜单ID列表
     * @param menuOpIds      菜单操作ID列表
     * @param operatorUserId 操作员用户ID
     */
    void replaceRoleMenus(Long roleId, List<Long> menuIds, List<Long> menuOpIds, Long operatorUserId);

    /**
     * 替换角色数据资源接口授权
     *
     * @param roleId           角色ID
     * @param drsInterfaceIds  数据资源接口ID列表
     * @param operatorUserId   操作员用户ID
     */
    void replaceRoleDrsInterfaces(Long roleId, List<Long> drsInterfaceIds, Long operatorUserId);

    /**
     * 根据系统ID查询系统菜单列表
     *
     * @param systemId 系统ID
     * @return 系统菜单列表
     */
    List<MenuEntity> listMenusBySystemId(Long systemId);

    /**
     * 根据菜单ID查询菜单
     *
     * @param menuId 菜单ID
     * @return 菜单
     */
    MenuEntity findMenuById(Long menuId);

    /**
     * 根据系统ID、菜单编码统计菜单数量
     *
     * @param systemId      系统ID
     * @param menuCode      菜单编码
     * @param excludeMenuId 排除菜单ID
     * @return 菜单数量
     */
    long countMenuCode(Long systemId, String menuCode, Long excludeMenuId);

    /**
     * 插入菜单
     *
     * @param menu 菜单
     * @return 菜单
     */
    MenuEntity insertMenu(MenuEntity menu);

    /**
     * 更新菜单
     *
     * @param menu 菜单
     */
    void updateMenu(MenuEntity menu);

    /**
     * 删除菜单（逻辑删除）
     *
     * @param menuId 菜单ID
     */
    void deleteMenuById(Long menuId);

    /**
     * 统计子菜单数量
     *
     * @param menuId 菜单ID
     * @return 子菜单数量
     */
    long countChildMenus(Long menuId);

    /**
     * 根据菜单ID统计菜单操作数量
     *
     * @param menuId 菜单ID
     * @return 菜单操作数量
     */
    long countMenuOpsByMenuId(Long menuId);

    /**
     * 删除菜单绑定的角色菜单关系（物理删除）
     *
     * @param menuId 菜单ID
     */
    void deleteRoleMenuBindingsByMenuId(Long menuId);

    /**
     * 根据菜单ID查询菜单操作列表
     *
     * @param menuId 菜单ID
     * @return 菜单操作列表
     */
    PageResponse<MenuOpEntity> listMenuOpsByMenuId(Long menuId, int currentPage, int pageSize);

    /**
     * 根据系统ID查询系统菜单操作列表
     *
     * @param systemId 系统ID
     * @return 系统菜单操作列表
     */
    List<MenuOpEntity> listMenuOpsBySystemId(Long systemId);

    /**
     * 根据菜单操作ID查询菜单操作
     *
     * @param menuOpId 菜单操作ID
     * @return 菜单操作
     */
    MenuOpEntity findMenuOpById(Long menuOpId);

    /**
     * 根据菜单ID、操作编码统计菜单操作数量
     *
     * @param menuId          菜单ID
     * @param opCode          操作编码
     * @param excludeMenuOpId 排除菜单操作ID
     * @return 菜单操作数量
     */
    long countMenuOpCode(Long menuId, String opCode, Long excludeMenuOpId);

    /**
     * 插入菜单操作
     *
     * @param menuOp 菜单操作
     * @return 菜单操作
     */
    MenuOpEntity insertMenuOp(MenuOpEntity menuOp);

    /**
     * 更新菜单操作
     *
     * @param menuOp 菜单操作
     */
    void updateMenuOp(MenuOpEntity menuOp);

    /**
     * 删除菜单操作（逻辑删除）
     *
     * @param menuOpId 菜单操作ID
     */
    void deleteMenuOpById(Long menuOpId);

    /**
     * 删除菜单操作绑定的角色授权（物理删除）
     *
     * @param menuOpId 菜单操作ID
     */
    void deleteRoleMenuOpBindingsByMenuOpId(Long menuOpId);

    /**
     * 根据系统ID、菜单ID列表统计菜单数量
     *
     * @param systemId 系统ID
     * @param menuIds  菜单ID列表
     * @return 菜单数量
     */
    long countMenusByIdsAndSystemId(Long systemId, List<Long> menuIds);

    /**
     * 根据系统ID、菜单操作ID列表统计菜单操作数量
     *
     * @param systemId  系统ID
     * @param menuOpIds 菜单操作ID列表
     * @return 菜单操作数量
     */
    long countMenuOpsByIdsAndSystemId(Long systemId, List<Long> menuOpIds);

    /**
     * 根据系统ID、数据资源接口ID列表统计数据资源接口数量
     *
     * @param systemId         系统ID
     * @param drsInterfaceIds  数据资源接口ID列表
     * @return 数据资源接口数量
     */
    long countDrsInterfacesByIdsAndSystemId(Long systemId, List<Long> drsInterfaceIds);
}
