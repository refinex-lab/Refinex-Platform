package cn.refinex.ai.infrastructure.persistence.mapper;

import cn.refinex.ai.infrastructure.persistence.dataobject.AiSkillDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI技能 Mapper
 *
 * @author refinex
 */
@Mapper
public interface AiSkillMapper extends BaseMapper<AiSkillDo> {
}
