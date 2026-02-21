package cn.refinex.ai.infrastructure.persistence.mapper;

import cn.refinex.ai.infrastructure.persistence.dataobject.AiMcpServerDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MCP服务器 Mapper
 *
 * @author refinex
 */
@Mapper
public interface AiMcpServerMapper extends BaseMapper<AiMcpServerDo> {
}
