package cn.refinex.ai.infrastructure.converter;

import cn.refinex.ai.domain.model.entity.UsageLogEntity;
import cn.refinex.ai.infrastructure.persistence.dataobject.AiUsageLogDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 调用日志 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface UsageLogDoConverter {

    /**
     * 转换为调用日志实体
     *
     * @param usageLogDo 调用日志数据对象
     * @return 调用日志实体
     */
    UsageLogEntity toEntity(AiUsageLogDo usageLogDo);

    /**
     * 转换为调用日志数据对象
     *
     * @param usageLogEntity 调用日志实体
     * @return 调用日志数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    AiUsageLogDo toDo(UsageLogEntity usageLogEntity);
}
