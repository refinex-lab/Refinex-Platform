package cn.refinex.ai.infrastructure.converter;

import cn.refinex.ai.domain.model.entity.ProviderEntity;
import cn.refinex.ai.infrastructure.persistence.dataobject.AiProviderDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 供应商 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface ProviderDoConverter {

    /**
     * 转换为供应商实体
     *
     * @param providerDo 供应商数据对象
     * @return 供应商实体
     */
    ProviderEntity toEntity(AiProviderDo providerDo);

    /**
     * 转换为供应商数据对象
     *
     * @param providerEntity 供应商实体
     * @return 供应商数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    AiProviderDo toDo(ProviderEntity providerEntity);
}
