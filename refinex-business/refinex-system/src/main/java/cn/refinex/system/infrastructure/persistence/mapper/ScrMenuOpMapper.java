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
     * 按企业+系统查询菜单操作
     */
    @Select("""
            SELECT mo.*
            FROM scr_menu_op mo
            JOIN scr_menu m ON m.id = mo.menu_id
            WHERE m.estab_id = #{estabId}
              AND m.system_id = #{systemId}
              AND m.deleted = 0
              AND mo.deleted = 0
            ORDER BY m.sort ASC, m.id ASC, mo.sort ASC, mo.id ASC
            """)
    List<ScrMenuOpDo> selectByEstabAndSystemId(@Param("estabId") Long estabId,
                                               @Param("systemId") Long systemId);

    /**
     * 按多个企业ID查询菜单操作
     */
    @Select({
            "<script>",
            "SELECT mo.*",
            "FROM scr_menu_op mo",
            "JOIN scr_menu m ON m.id = mo.menu_id",
            "WHERE m.estab_id IN",
            "  <foreach collection='estabIds' item='eid' open='(' separator=',' close=')'>",
            "    #{eid}",
            "  </foreach>",
            "  AND m.deleted = 0",
            "  AND mo.deleted = 0",
            "ORDER BY m.sort ASC, m.id ASC, mo.sort ASC, mo.id ASC",
            "</script>"
    })
    List<ScrMenuOpDo> selectByEstabIds(@Param("estabIds") List<Long> estabIds);

    /**
     * 按企业统计菜单操作数量
     */
    @Select({
            "<script>",
            "SELECT COUNT(1)",
            "FROM scr_menu_op mo",
            "JOIN scr_menu m ON m.id = mo.menu_id",
            "WHERE m.estab_id = #{estabId}",
            "  AND m.deleted = 0",
            "  AND mo.deleted = 0",
            "  AND mo.id IN",
            "  <foreach collection='menuOpIds' item='id' open='(' separator=',' close=')'>",
            "    #{id}",
            "  </foreach>",
            "</script>"
    })
    long countByIdsAndEstabId(@Param("estabId") Long estabId, @Param("menuOpIds") List<Long> menuOpIds);
}
