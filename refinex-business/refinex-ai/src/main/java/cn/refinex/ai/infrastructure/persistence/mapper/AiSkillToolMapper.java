package cn.refinex.ai.infrastructure.persistence.mapper;

import cn.refinex.ai.infrastructure.persistence.dataobject.AiSkillToolDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 技能-工具关联 Mapper
 *
 * @author refinex
 */
@Mapper
public interface AiSkillToolMapper extends BaseMapper<AiSkillToolDo> {
}
