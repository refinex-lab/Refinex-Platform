package cn.refinex.system.infrastructure.persistence.mapper;

import cn.refinex.system.infrastructure.persistence.dataobject.ScrRoleMenuOpDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色菜单操作授权 Mapper
 *
 * @author refinex
 */
@Mapper
public interface ScrRoleMenuOpMapper extends BaseMapper<ScrRoleMenuOpDo> {

    /**
     * 查询角色授权菜单操作ID
     *
     * @param roleId 角色ID
     * @return 菜单操作ID列表
     */
    @Select("""
            SELECT menu_op_id
            FROM scr_role_menu_op
            WHERE role_id = #{roleId}
              AND deleted = 0
            ORDER BY id ASC
            """)
    List<Long> selectMenuOpIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 物理删除角色菜单操作授权
     *
     * @param roleId 角色ID
     * @return 影响行数
     */
    @Delete("DELETE FROM scr_role_menu_op WHERE role_id = #{roleId}")
    int deleteByRoleIdHard(@Param("roleId") Long roleId);

    /**
     * 物理删除菜单操作绑定的角色菜单操作授权
     *
     * @param menuOpId 菜单操作ID
     * @return 影响行数
     */
    @Delete("DELETE FROM scr_role_menu_op WHERE menu_op_id = #{menuOpId}")
    int deleteByMenuOpIdHard(@Param("menuOpId") Long menuOpId);
}
