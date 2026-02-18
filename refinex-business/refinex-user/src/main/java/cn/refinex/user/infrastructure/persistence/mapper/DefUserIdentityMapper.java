package cn.refinex.user.infrastructure.persistence.mapper;

import cn.refinex.user.infrastructure.persistence.dataobject.DefUserIdentityDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户身份 Mapper
 *
 * @author refinex
 */
@Mapper
public interface DefUserIdentityMapper extends BaseMapper<DefUserIdentityDo> {
}
