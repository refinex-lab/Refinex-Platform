package cn.refinex.ai.infrastructure.persistence.mapper;

import cn.refinex.ai.infrastructure.persistence.dataobject.AiSkillKnowledgeDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 技能-知识库关联 Mapper
 *
 * @author refinex
 */
@Mapper
public interface AiSkillKnowledgeMapper extends BaseMapper<AiSkillKnowledgeDo> {
}
