package cn.refinex.auth.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.refinex.auth.domain.entity.DefUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * DefUser Mapper
 */
@Mapper
public interface DefUserMapper extends BaseMapper<DefUser> {
}
