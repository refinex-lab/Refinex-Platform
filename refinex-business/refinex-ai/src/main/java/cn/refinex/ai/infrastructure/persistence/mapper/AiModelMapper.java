package cn.refinex.ai.infrastructure.persistence.mapper;

import cn.refinex.ai.infrastructure.persistence.dataobject.AiModelDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 模型 Mapper
 *
 * @author refinex
 */
@Mapper
public interface AiModelMapper extends BaseMapper<AiModelDo> {
}
