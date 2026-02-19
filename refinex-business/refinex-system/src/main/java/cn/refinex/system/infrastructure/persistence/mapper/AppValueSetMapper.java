package cn.refinex.system.infrastructure.persistence.mapper;

import cn.refinex.system.infrastructure.persistence.dataobject.AppValueSetDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 值集定义 Mapper
 *
 * @author refinex
 */
@Mapper
public interface AppValueSetMapper extends BaseMapper<AppValueSetDo> {
}
