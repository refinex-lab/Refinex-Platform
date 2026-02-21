package cn.refinex.ai.infrastructure.persistence.mapper;

import cn.refinex.ai.infrastructure.persistence.dataobject.AiPromptTemplateDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Prompt模板 Mapper
 *
 * @author refinex
 */
@Mapper
public interface AiPromptTemplateMapper extends BaseMapper<AiPromptTemplateDo> {
}
