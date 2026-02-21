package cn.refinex.ai.infrastructure.converter;

import cn.refinex.ai.domain.model.entity.PromptTemplateEntity;
import cn.refinex.ai.infrastructure.persistence.dataobject.AiPromptTemplateDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Prompt模板 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface PromptTemplateDoConverter {

    /**
     * 转换为Prompt模板实体
     *
     * @param promptTemplateDo Prompt模板数据对象
     * @return Prompt模板实体
     */
    PromptTemplateEntity toEntity(AiPromptTemplateDo promptTemplateDo);

    /**
     * 转换为Prompt模板数据对象
     *
     * @param promptTemplateEntity Prompt模板实体
     * @return Prompt模板数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    AiPromptTemplateDo toDo(PromptTemplateEntity promptTemplateEntity);
}
