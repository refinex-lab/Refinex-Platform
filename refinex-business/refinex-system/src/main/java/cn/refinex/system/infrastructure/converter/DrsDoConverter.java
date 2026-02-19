package cn.refinex.system.infrastructure.converter;

import cn.refinex.system.domain.model.entity.DrsEntity;
import cn.refinex.system.infrastructure.persistence.dataobject.ScrDrsDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 数据资源 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface DrsDoConverter {

    /**
     * 转换为数据资源实体
     *
     * @param drsDo 数据资源数据对象
     * @return 数据资源实体
     */
    DrsEntity toEntity(ScrDrsDo drsDo);

    /**
     * 转换为数据资源数据对象
     *
     * @param drsEntity 数据资源实体
     * @return 数据资源数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    ScrDrsDo toDo(DrsEntity drsEntity);
}
