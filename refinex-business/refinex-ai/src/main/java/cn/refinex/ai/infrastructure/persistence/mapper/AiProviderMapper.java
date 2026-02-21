package cn.refinex.ai.infrastructure.persistence.mapper;

import cn.refinex.ai.infrastructure.persistence.dataobject.AiProviderDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 供应商 Mapper
 *
 * @author refinex
 */
@Mapper
public interface AiProviderMapper extends BaseMapper<AiProviderDo> {
}
