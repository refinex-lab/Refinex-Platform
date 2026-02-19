package cn.refinex.system.infrastructure.persistence.mapper;

import cn.refinex.system.infrastructure.persistence.dataobject.ScrRoleUserDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色用户关系 Mapper
 *
 * @author refinex
 */
@Mapper
public interface ScrRoleUserMapper extends BaseMapper<ScrRoleUserDo> {

    /**
     * 查询角色授权用户（仅有效记录）
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    @Select("""
            SELECT user_id
            FROM scr_role_user
            WHERE role_id = #{roleId}
              AND deleted = 0
              AND status = 1
            ORDER BY id ASC
            """)
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 物理删除角色用户关系
     *
     * @param roleId 角色ID
     * @return 影响行数
     */
    @Delete("DELETE FROM scr_role_user WHERE role_id = #{roleId}")
    int deleteByRoleIdHard(@Param("roleId") Long roleId);
}
