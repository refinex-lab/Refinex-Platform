package cn.refinex.system.infrastructure.converter;

import cn.refinex.system.domain.model.entity.ValueEntity;
import cn.refinex.system.infrastructure.persistence.dataobject.AppValueDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 值集明细 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface ValueDoConverter {

    /**
     * 转换为值集明细实体
     *
     * @param valueDo 值集明细数据对象
     * @return 值集明细实体
     */
    ValueEntity toEntity(AppValueDo valueDo);

    /**
     * 转换为值集明细数据对象
     *
     * @param valueEntity 值集明细实体
     * @return 值集明细数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    AppValueDo toDo(ValueEntity valueEntity);
}
