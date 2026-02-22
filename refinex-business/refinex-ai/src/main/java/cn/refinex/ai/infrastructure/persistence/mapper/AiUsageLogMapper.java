package cn.refinex.ai.infrastructure.persistence.mapper;

import cn.refinex.ai.infrastructure.persistence.dataobject.AiUsageLogDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI调用日志 Mapper
 *
 * @author refinex
 */
@Mapper
public interface AiUsageLogMapper extends BaseMapper<AiUsageLogDo> {
}
