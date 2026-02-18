package cn.refinex.user.infrastructure.persistence.mapper;

import cn.refinex.user.infrastructure.persistence.dataobject.DefEstabUserDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
