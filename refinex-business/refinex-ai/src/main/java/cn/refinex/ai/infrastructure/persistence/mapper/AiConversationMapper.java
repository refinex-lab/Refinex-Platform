package cn.refinex.ai.infrastructure.persistence.mapper;

import cn.refinex.ai.infrastructure.persistence.dataobject.AiConversationDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI对话 Mapper
 *
 * @author refinex
 */
@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversationDo> {
}
