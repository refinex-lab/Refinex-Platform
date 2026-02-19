package cn.refinex.system.infrastructure.persistence.mapper;

import cn.refinex.system.infrastructure.persistence.dataobject.ScrSystemDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统定义 Mapper
 *
 * @author refinex
 */
@Mapper
public interface ScrSystemMapper extends BaseMapper<ScrSystemDo> {
}
