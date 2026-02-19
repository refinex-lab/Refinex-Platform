package cn.refinex.system.infrastructure.persistence.mapper;

import cn.refinex.system.infrastructure.persistence.dataobject.DefTeamDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 团队 Mapper
 *
 * @author refinex
 */
@Mapper
public interface DefTeamMapper extends BaseMapper<DefTeamDo> {
}
