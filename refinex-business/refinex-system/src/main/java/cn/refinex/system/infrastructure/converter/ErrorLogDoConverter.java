package cn.refinex.system.infrastructure.converter;

import cn.refinex.system.domain.model.entity.ErrorLogEntity;
import cn.refinex.system.infrastructure.persistence.dataobject.LogErrorDo;
import org.mapstruct.Mapper;

/**
 * 错误日志 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface ErrorLogDoConverter {

    /**
     * 转换为错误日志实体
     *
     * @param errorLogDo 错误日志数据对象
     * @return 错误日志实体
     */
    ErrorLogEntity toEntity(LogErrorDo errorLogDo);
}
