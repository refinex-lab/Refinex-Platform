package cn.refinex.auth.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.refinex.auth.domain.entity.DefUserIdentity;
import org.apache.ibatis.annotations.Mapper;

/**
 * DefUserIdentity Mapper
 */
@Mapper
public interface DefUserIdentityMapper extends BaseMapper<DefUserIdentity> {
}
