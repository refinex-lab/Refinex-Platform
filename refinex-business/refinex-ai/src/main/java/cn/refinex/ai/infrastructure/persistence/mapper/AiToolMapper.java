package cn.refinex.ai.infrastructure.persistence.mapper;

import cn.refinex.ai.infrastructure.persistence.dataobject.AiToolDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI工具 Mapper
 *
 * @author refinex
 */
@Mapper
public interface AiToolMapper extends BaseMapper<AiToolDo> {
}
