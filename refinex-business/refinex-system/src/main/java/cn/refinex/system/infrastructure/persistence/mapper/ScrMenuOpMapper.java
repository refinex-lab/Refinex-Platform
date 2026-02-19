package cn.refinex.system.infrastructure.persistence.mapper;

import cn.refinex.system.infrastructure.persistence.dataobject.ScrMenuOpDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单操作 Mapper
 *
 * @author refinex
 */
@Mapper
public interface ScrMenuOpMapper extends BaseMapper<ScrMenuOpDo> {

    /**
     * 按系统查询菜单操作
     *
     * @param systemId 系统ID
     * @return 菜单操作列表
     */
    @Select("""
            SELECT mo.*
            FROM scr_menu_op mo
            JOIN scr_menu m ON m.id = mo.menu_id
            WHERE m.system_id = #{systemId}
              AND m.deleted = 0
              AND mo.deleted = 0
            ORDER BY m.sort ASC, m.id ASC, mo.sort ASC, mo.id ASC
            """)
    List<ScrMenuOpDo> selectBySystemId(@Param("systemId") Long systemId);

    /**
     * 按系统统计菜单操作数量
     *
     * @param systemId 系统ID
     * @param menuOpIds 菜单操作ID列表
     * @return 数量
     */
    @Select({
            "<script>",
            "SELECT COUNT(1)",
            "FROM scr_menu_op mo",
            "JOIN scr_menu m ON m.id = mo.menu_id",
            "WHERE m.system_id = #{systemId}",
            "  AND m.deleted = 0",
            "  AND mo.deleted = 0",
            "  AND mo.id IN",
            "  <foreach collection='menuOpIds' item='id' open='(' separator=',' close=')'>",
            "    #{id}",
            "  </foreach>",
            "</script>"
    })
    long countByIdsAndSystemId(@Param("systemId") Long systemId, @Param("menuOpIds") List<Long> menuOpIds);
}
