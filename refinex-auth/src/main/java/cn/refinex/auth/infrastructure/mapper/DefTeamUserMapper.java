package cn.refinex.auth.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.refinex.auth.domain.entity.DefTeamUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * DefTeamUser Mapper
 */
@Mapper
public interface DefTeamUserMapper extends BaseMapper<DefTeamUser> {

    @Select("SELECT team_id FROM def_team_user WHERE user_id = #{userId} AND status = 1 AND deleted = 0 ORDER BY id LIMIT 1")
    Long selectFirstTeamId(@Param("userId") Long userId);
}
