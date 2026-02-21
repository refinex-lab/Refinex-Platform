package cn.refinex.system.infrastructure.persistence.mapper;

import cn.refinex.system.infrastructure.persistence.dataobject.DefOpDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作定义 Mapper
 *
 * @author refinex
 */
@Mapper
public interface DefOpMapper extends BaseMapper<DefOpDo> {
}
