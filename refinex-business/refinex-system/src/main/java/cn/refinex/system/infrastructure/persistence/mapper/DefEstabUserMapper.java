package cn.refinex.system.infrastructure.persistence.mapper;

import cn.refinex.system.infrastructure.persistence.dataobject.DefEstabUserDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业成员关系 Mapper
 *
 * @author refinex
 */
@Mapper
public interface DefEstabUserMapper extends BaseMapper<DefEstabUserDo> {
}
