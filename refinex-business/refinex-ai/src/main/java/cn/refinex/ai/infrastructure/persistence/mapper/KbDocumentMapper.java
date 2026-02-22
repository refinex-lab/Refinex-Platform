package cn.refinex.ai.infrastructure.persistence.mapper;

import cn.refinex.ai.infrastructure.persistence.dataobject.KbDocumentDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库文档 Mapper
 *
 * @author refinex
 */
@Mapper
public interface KbDocumentMapper extends BaseMapper<KbDocumentDo> {
}
