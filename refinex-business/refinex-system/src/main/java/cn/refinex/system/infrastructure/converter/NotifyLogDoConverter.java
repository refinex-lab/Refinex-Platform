package cn.refinex.system.infrastructure.converter;

import cn.refinex.system.domain.model.entity.NotifyLogEntity;
import cn.refinex.system.infrastructure.persistence.dataobject.LogNotifyDo;
import org.mapstruct.Mapper;

/**
 * 通知日志 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface NotifyLogDoConverter {

    /**
     * 转换为通知日志实体
     *
     * @param notifyLogDo 通知日志数据对象
     * @return 通知日志实体
     */
    NotifyLogEntity toEntity(LogNotifyDo notifyLogDo);
}
