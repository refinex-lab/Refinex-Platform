package cn.refinex.auth.infrastructure.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * RBAC 聚合查询
 *
 * @author refinex
 */
@Mapper
public interface AuthRbacMapper {

    /**
     * 查询用户角色
     *
     * @param userId   用户ID
     * @param estabId  机构ID
     * @param systemId 系统ID
     * @return 角色集合
     */
    @Select({
            "<script>",
            "SELECT DISTINCT r.role_code",
            "FROM scr_role_user ru",
            "JOIN scr_role r ON ru.role_id = r.id",
            "WHERE ru.user_id = #{userId}",
            "  AND ru.deleted = 0 AND r.deleted = 0",
            "  AND ru.status = 1 AND r.status = 1",
            "  AND (ru.estab_id = #{estabId} OR ru.estab_id = 0)",
            "<if test='systemId != null'>",
            "  AND r.system_id = #{systemId}",
            "</if>",
            "</script>"
    })
    List<String> findRoleCodes(@Param("userId") Long userId,
                               @Param("estabId") Long estabId,
                               @Param("systemId") Long systemId);

    /**
     * 查询用户权限
     *
     * @param userId   用户ID
     * @param estabId  机构ID
     * @param systemId 系统ID
     * @return 权限集合
     */
    @Select({
            "<script>",
            "SELECT DISTINCT m.permission_key",
            "FROM scr_role_user ru",
            "JOIN scr_role r ON ru.role_id = r.id",
            "JOIN scr_role_menu rm ON rm.role_id = r.id",
            "JOIN scr_menu m ON m.id = rm.menu_id",
            "WHERE ru.user_id = #{userId}",
            "  AND ru.deleted = 0 AND r.deleted = 0 AND rm.deleted = 0 AND m.deleted = 0",
            "  AND ru.status = 1 AND r.status = 1 AND m.status = 1",
            "  AND (ru.estab_id = #{estabId} OR ru.estab_id = 0)",
            "  AND m.permission_key IS NOT NULL AND m.permission_key &lt;&gt; ''",
            "<if test='systemId != null'>",
            "  AND m.system_id = #{systemId}",
            "</if>",
            "UNION",
            "SELECT DISTINCT mo.permission_key",
            "FROM scr_role_user ru",
            "JOIN scr_role r ON ru.role_id = r.id",
            "JOIN scr_role_menu_op rmo ON rmo.role_id = r.id",
            "JOIN scr_menu_op mo ON mo.id = rmo.menu_op_id",
            "JOIN scr_menu m2 ON m2.id = mo.menu_id",
            "WHERE ru.user_id = #{userId}",
            "  AND ru.deleted = 0 AND r.deleted = 0 AND rmo.deleted = 0 AND mo.deleted = 0 AND m2.deleted = 0",
            "  AND ru.status = 1 AND r.status = 1 AND mo.status = 1 AND m2.status = 1",
            "  AND (ru.estab_id = #{estabId} OR ru.estab_id = 0)",
            "  AND mo.permission_key IS NOT NULL AND mo.permission_key &lt;&gt; ''",
            "<if test='systemId != null'>",
            "  AND m2.system_id = #{systemId}",
            "</if>",
            "UNION",
            "SELECT DISTINCT di.permission_key",
            "FROM scr_role_user ru",
            "JOIN scr_role r ON ru.role_id = r.id",
            "JOIN scr_role_drs_interface rdi ON rdi.role_id = r.id",
            "JOIN scr_drs_interface di ON di.id = rdi.drs_interface_id",
            "JOIN scr_drs d ON d.id = di.drs_id",
            "WHERE ru.user_id = #{userId}",
            "  AND ru.deleted = 0 AND r.deleted = 0 AND rdi.deleted = 0 AND di.deleted = 0 AND d.deleted = 0",
            "  AND ru.status = 1 AND r.status = 1 AND di.status = 1 AND d.status = 1",
            "  AND (ru.estab_id = #{estabId} OR ru.estab_id = 0)",
            "  AND di.permission_key IS NOT NULL AND di.permission_key &lt;&gt; ''",
            "<if test='systemId != null'>",
            "  AND d.system_id = #{systemId}",
            "</if>",
            "</script>"
    })
    List<String> findPermissionKeys(@Param("userId") Long userId,
                                    @Param("estabId") Long estabId,
                                    @Param("systemId") Long systemId);
}
