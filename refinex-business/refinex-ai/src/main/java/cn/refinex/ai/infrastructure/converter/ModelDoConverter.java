package cn.refinex.ai.infrastructure.converter;

import cn.refinex.ai.domain.model.entity.ModelEntity;
import cn.refinex.ai.infrastructure.persistence.dataobject.AiModelDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 模型 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface ModelDoConverter {

    /**
     * DO 转换为实体
     *
     * @param modelDo 模型 DO
     * @return 模型实体
     */
    ModelEntity toEntity(AiModelDo modelDo);

    /**
     * 实体转换为 DO
     *
     * @param modelEntity 模型实体
     * @return 模型 DO
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    AiModelDo toDo(ModelEntity modelEntity);
}
