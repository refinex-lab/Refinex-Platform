package cn.refinex.system.infrastructure.converter;

import cn.refinex.system.domain.model.entity.DrsInterfaceEntity;
import cn.refinex.system.infrastructure.persistence.dataobject.ScrDrsInterfaceDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 数据资源接口 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface DrsInterfaceDoConverter {

    /**
     * 转换为数据资源接口实体
     *
     * @param drsInterfaceDo 数据资源接口数据对象
     * @return 数据资源接口实体
     */
    DrsInterfaceEntity toEntity(ScrDrsInterfaceDo drsInterfaceDo);

    /**
     * 转换为数据资源接口数据对象
     *
     * @param drsInterfaceEntity 数据资源接口实体
     * @return 数据资源接口数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    ScrDrsInterfaceDo toDo(DrsInterfaceEntity drsInterfaceEntity);
}
