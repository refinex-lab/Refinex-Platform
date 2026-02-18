package cn.refinex.user.infrastructure.persistence.mapper;

import cn.refinex.user.infrastructure.persistence.dataobject.DefEstabUserDo;
import cn.refinex.user.infrastructure.persistence.dataobject.UserEstabJoinDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 组织成员关系 Mapper
 *
 * @author refinex
 */
@Mapper
public interface DefEstabUserMapper extends BaseMapper<DefEstabUserDo> {

    /**
     * 根据用户 ID 和企业 ID 查询
     *
     * @param userId  用户 ID
     * @param estabId 企业 ID
     * @return 组织成员关系
     */
    @Select("SELECT * FROM def_estab_user WHERE user_id = #{userId} AND estab_id = #{estabId} AND status = 1 AND deleted = 0 LIMIT 1")
    DefEstabUserDo selectActive(@Param("userId") Long userId, @Param("estabId") Long estabId);

    /**
     * 查询用户所属有效企业列表
     *
     * @param userId 用户ID
     * @return 企业列表
     */
    @Select("""
            SELECT eu.estab_id AS estabId,
                   eu.is_admin AS isAdmin,
                   e.estab_code AS estabCode,
                   e.estab_name AS estabName,
                   e.estab_short_name AS estabShortName,
                   e.logo_url AS logoUrl,
                   e.estab_type AS estabType
            FROM def_estab_user eu
            JOIN def_estab e ON e.id = eu.estab_id
            WHERE eu.user_id = #{userId}
              AND eu.status = 1
              AND eu.deleted = 0
              AND e.status = 1
              AND e.deleted = 0
            ORDER BY eu.is_admin DESC, eu.id ASC
            """)
    List<UserEstabJoinDo> selectActiveEstabs(@Param("userId") Long userId);
}
