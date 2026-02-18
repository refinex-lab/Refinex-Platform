package cn.refinex.user.infrastructure.persistence.mapper;

import cn.refinex.user.infrastructure.persistence.dataobject.DefTeamUserDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 团队成员关系 Mapper
 *
 * @author refinex
 */
@Mapper
public interface DefTeamUserMapper extends BaseMapper<DefTeamUserDo> {

    /**
     * 根据用户 ID 查询第一个团队 ID
     *
     * @param userId 用户 ID
     * @return 团队 ID
     */
    @Select("SELECT team_id FROM def_team_user WHERE user_id = #{userId} AND status = 1 AND deleted = 0 ORDER BY id LIMIT 1")
    Long selectFirstTeamId(@Param("userId") Long userId);
}
