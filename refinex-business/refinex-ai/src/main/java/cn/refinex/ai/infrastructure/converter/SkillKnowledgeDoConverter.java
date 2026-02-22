package cn.refinex.ai.infrastructure.converter;

import cn.refinex.ai.domain.model.entity.SkillKnowledgeEntity;
import cn.refinex.ai.infrastructure.persistence.dataobject.AiSkillKnowledgeDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 技能-知识库关联 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface SkillKnowledgeDoConverter {

    /**
     * 转换为技能知识库关联实体
     *
     * @param skillKnowledgeDo 技能知识库关联数据对象
     * @return 技能知识库关联实体
     */
    SkillKnowledgeEntity toEntity(AiSkillKnowledgeDo skillKnowledgeDo);

    /**
     * 转换为技能知识库关联数据对象
     *
     * @param skillKnowledgeEntity 技能知识库关联实体
     * @return 技能知识库关联数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    AiSkillKnowledgeDo toDo(SkillKnowledgeEntity skillKnowledgeEntity);
}
