package cn.refinex.auth.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.refinex.auth.domain.entity.LogLogin;
import org.apache.ibatis.annotations.Mapper;

/**
 * LogLogin Mapper
 */
@Mapper
public interface LogLoginMapper extends BaseMapper<LogLogin> {
}
