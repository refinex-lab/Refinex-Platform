package cn.refinex.system.infrastructure.persistence.mapper;

import cn.refinex.system.infrastructure.persistence.dataobject.ScrRoleDrsInterfaceDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色数据资源授权 Mapper
 *
 * @author refinex
 */
@Mapper
public interface ScrRoleDrsInterfaceMapper extends BaseMapper<ScrRoleDrsInterfaceDo> {

    /**
     * 查询角色授权数据资源接口ID
     */
    @Select("""
            SELECT drs_interface_id
            FROM scr_role_drs
            WHERE role_id = #{roleId}
              AND deleted = 0
            ORDER BY id ASC
            """)
    List<Long> selectDrsInterfaceIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 物理删除角色数据资源授权
     */
    @Delete("DELETE FROM scr_role_drs WHERE role_id = #{roleId}")
    int deleteByRoleIdHard(@Param("roleId") Long roleId);
}
