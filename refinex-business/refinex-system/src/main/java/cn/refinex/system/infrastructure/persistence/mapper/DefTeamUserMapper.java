package cn.refinex.system.infrastructure.persistence.mapper;

import cn.refinex.system.infrastructure.persistence.dataobject.DefTeamUserDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 团队成员 Mapper
 *
 * @author refinex
 */
@Mapper
public interface DefTeamUserMapper extends BaseMapper<DefTeamUserDo> {
}
