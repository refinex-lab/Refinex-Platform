package cn.refinex.system.infrastructure.persistence.mapper;

import cn.refinex.system.infrastructure.persistence.dataobject.LogErrorDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 错误日志 Mapper
 *
 * @author refinex
 */
@Mapper
public interface LogErrorMapper extends BaseMapper<LogErrorDo> {
}
