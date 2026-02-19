package cn.refinex.system.infrastructure.converter;

import cn.refinex.system.domain.model.entity.SystemEntity;
import cn.refinex.system.infrastructure.persistence.dataobject.ScrSystemDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 系统 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface SystemDoConverter {

    /**
     * 转换为系统实体
     *
     * @param systemDo 系统数据对象
     * @return 系统实体
     */
    SystemEntity toEntity(ScrSystemDo systemDo);

    /**
     * 转换为系统数据对象
     *
     * @param systemEntity 系统实体
     * @return 系统数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    ScrSystemDo toDo(SystemEntity systemEntity);
}
