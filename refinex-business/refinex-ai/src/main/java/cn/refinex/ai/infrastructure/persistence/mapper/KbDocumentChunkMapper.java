package cn.refinex.ai.infrastructure.persistence.mapper;

import cn.refinex.ai.infrastructure.persistence.dataobject.KbDocumentChunkDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档切片 Mapper
 *
 * @author refinex
 */
@Mapper
public interface KbDocumentChunkMapper extends BaseMapper<KbDocumentChunkDo> {
}
