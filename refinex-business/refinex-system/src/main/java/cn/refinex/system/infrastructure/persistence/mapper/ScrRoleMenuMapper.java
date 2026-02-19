package cn.refinex.system.infrastructure.persistence.mapper;

import cn.refinex.system.infrastructure.persistence.dataobject.ScrRoleMenuDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色菜单授权 Mapper
 *
 * @author refinex
 */
@Mapper
public interface ScrRoleMenuMapper extends BaseMapper<ScrRoleMenuDo> {

    /**
     * 查询角色授权菜单ID
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    @Select("""
            SELECT menu_id
            FROM scr_role_menu
            WHERE role_id = #{roleId}
              AND deleted = 0
            ORDER BY id ASC
            """)
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 物理删除角色菜单授权
     *
     * @param roleId 角色ID
     * @return 影响行数
     */
    @Delete("DELETE FROM scr_role_menu WHERE role_id = #{roleId}")
    int deleteByRoleIdHard(@Param("roleId") Long roleId);

    /**
     * 物理删除菜单绑定的角色菜单授权
     *
     * @param menuId 菜单ID
     * @return 影响行数
     */
    @Delete("DELETE FROM scr_role_menu WHERE menu_id = #{menuId}")
    int deleteByMenuIdHard(@Param("menuId") Long menuId);
}
