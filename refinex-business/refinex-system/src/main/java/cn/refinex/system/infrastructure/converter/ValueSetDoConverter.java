package cn.refinex.system.infrastructure.converter;

import cn.refinex.system.domain.model.entity.ValueSetEntity;
import cn.refinex.system.infrastructure.persistence.dataobject.AppValueSetDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 值集 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface ValueSetDoConverter {

    /**
     * 转换为值集实体
     *
     * @param valueSetDo 值集数据对象
     * @return 值集实体
     */
    ValueSetEntity toEntity(AppValueSetDo valueSetDo);

    /**
     * 转换为值集数据对象
     *
     * @param valueSetEntity 值集实体
     * @return 值集数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    AppValueSetDo toDo(ValueSetEntity valueSetEntity);
}
