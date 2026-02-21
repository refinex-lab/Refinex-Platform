package cn.refinex.ai.infrastructure.converter;

import cn.refinex.ai.domain.model.entity.SkillEntity;
import cn.refinex.ai.infrastructure.persistence.dataobject.AiSkillDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * AI技能 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface SkillDoConverter {

    /**
     * 转换为技能实体
     *
     * @param skillDo 技能数据对象
     * @return 技能实体
     */
    SkillEntity toEntity(AiSkillDo skillDo);

    /**
     * 转换为技能数据对象
     *
     * @param skillEntity 技能实体
     * @return 技能数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    AiSkillDo toDo(SkillEntity skillEntity);
}
