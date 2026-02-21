package cn.refinex.ai.infrastructure.converter;

import cn.refinex.ai.domain.model.entity.ModelProvisionEntity;
import cn.refinex.ai.infrastructure.persistence.dataobject.AiModelProvisionDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 租户模型开通 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface ModelProvisionDoConverter {

    /**
     * 转换为租户模型开通实体
     *
     * @param modelProvisionDo 租户模型开通数据对象
     * @return 租户模型开通实体
     */
    ModelProvisionEntity toEntity(AiModelProvisionDo modelProvisionDo);

    /**
     * 转换为租户模型开通数据对象
     *
     * @param modelProvisionEntity 租户模型开通实体
     * @return 租户模型开通数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    AiModelProvisionDo toDo(ModelProvisionEntity modelProvisionEntity);
}
