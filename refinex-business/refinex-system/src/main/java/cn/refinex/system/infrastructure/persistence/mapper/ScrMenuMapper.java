package cn.refinex.system.infrastructure.persistence.mapper;

import cn.refinex.system.infrastructure.persistence.dataobject.ScrMenuDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单 Mapper
 *
 * @author refinex
 */
@Mapper
public interface ScrMenuMapper extends BaseMapper<ScrMenuDo> {
}
