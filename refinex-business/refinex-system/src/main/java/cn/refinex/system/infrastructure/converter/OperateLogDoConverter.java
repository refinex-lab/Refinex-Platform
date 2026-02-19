package cn.refinex.system.infrastructure.converter;

import cn.refinex.system.domain.model.entity.OperateLogEntity;
import cn.refinex.system.infrastructure.persistence.dataobject.LogOperateDo;
import org.mapstruct.Mapper;

/**
 * 操作日志 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface OperateLogDoConverter {

    /**
     * 转换为操作日志实体
     *
     * @param operateLogDo 操作日志数据对象
     * @return 操作日志实体
     */
    OperateLogEntity toEntity(LogOperateDo operateLogDo);
}
