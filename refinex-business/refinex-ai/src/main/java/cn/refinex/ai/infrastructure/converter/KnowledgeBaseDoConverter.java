package cn.refinex.ai.infrastructure.converter;

import cn.refinex.ai.domain.model.entity.KnowledgeBaseEntity;
import cn.refinex.ai.infrastructure.persistence.dataobject.KbKnowledgeBaseDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 知识库 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface KnowledgeBaseDoConverter {

    /**
     * 转换为知识库实体
     *
     * @param knowledgeBaseDo 知识库数据对象
     * @return 知识库实体
     */
    KnowledgeBaseEntity toEntity(KbKnowledgeBaseDo knowledgeBaseDo);

    /**
     * 转换为知识库数据对象
     *
     * @param knowledgeBaseEntity 知识库实体
     * @return 知识库数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    KbKnowledgeBaseDo toDo(KnowledgeBaseEntity knowledgeBaseEntity);
}
