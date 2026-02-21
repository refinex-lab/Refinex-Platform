package cn.refinex.ai.infrastructure.converter;

import cn.refinex.ai.domain.model.entity.ToolEntity;
import cn.refinex.ai.infrastructure.persistence.dataobject.AiToolDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * AI工具 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface ToolDoConverter {

    /**
     * 转换为工具实体
     *
     * @param toolDo 工具数据对象
     * @return 工具实体
     */
    ToolEntity toEntity(AiToolDo toolDo);

    /**
     * 转换为工具数据对象
     *
     * @param toolEntity 工具实体
     * @return 工具数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    AiToolDo toDo(ToolEntity toolEntity);
}
