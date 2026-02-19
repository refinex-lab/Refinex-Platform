package cn.refinex.system.infrastructure.persistence.mapper;

import cn.refinex.system.infrastructure.persistence.dataobject.LogNotifyDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知日志 Mapper
 *
 * @author refinex
 */
@Mapper
public interface LogNotifyMapper extends BaseMapper<LogNotifyDo> {
}
