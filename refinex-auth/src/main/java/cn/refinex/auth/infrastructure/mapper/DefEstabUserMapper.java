package cn.refinex.auth.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.refinex.auth.domain.entity.DefEstabUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * DefEstabUser Mapper
 */
@Mapper
public interface DefEstabUserMapper extends BaseMapper<DefEstabUser> {

    @Select("SELECT * FROM def_estab_user WHERE user_id = #{userId} AND estab_id = #{estabId} AND status = 1 AND deleted = 0 LIMIT 1")
    DefEstabUser selectActive(@Param("userId") Long userId, @Param("estabId") Long estabId);
}
