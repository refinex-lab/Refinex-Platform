package cn.refinex.user.infrastructure.persistence.mapper;

import cn.refinex.user.infrastructure.persistence.dataobject.DefUserDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 *
 * @author refinex
 */
@Mapper
public interface DefUserMapper extends BaseMapper<DefUserDo> {
}
