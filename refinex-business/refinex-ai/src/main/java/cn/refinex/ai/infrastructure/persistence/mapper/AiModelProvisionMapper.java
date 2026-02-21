package cn.refinex.ai.infrastructure.persistence.mapper;

import cn.refinex.ai.infrastructure.persistence.dataobject.AiModelProvisionDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户模型开通 Mapper
 *
 * @author refinex
 */
@Mapper
public interface AiModelProvisionMapper extends BaseMapper<AiModelProvisionDo> {
}
