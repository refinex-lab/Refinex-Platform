package cn.refinex.ai.infrastructure.persistence.mapper;

import cn.refinex.ai.infrastructure.persistence.dataobject.KbFolderDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库目录 Mapper
 *
 * @author refinex
 */
@Mapper
public interface KbFolderMapper extends BaseMapper<KbFolderDo> {
}
