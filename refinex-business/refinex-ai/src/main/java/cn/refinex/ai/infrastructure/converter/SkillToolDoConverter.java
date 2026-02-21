package cn.refinex.ai.infrastructure.converter;

import cn.refinex.ai.domain.model.entity.SkillToolEntity;
import cn.refinex.ai.infrastructure.persistence.dataobject.AiSkillToolDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 技能-工具关联 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface SkillToolDoConverter {

    /**
     * 转换为技能工具关联实体
     *
     * @param skillToolDo 技能工具关联数据对象
     * @return 技能工具关联实体
     */
    SkillToolEntity toEntity(AiSkillToolDo skillToolDo);

    /**
     * 转换为技能工具关联数据对象
     *
     * @param skillToolEntity 技能工具关联实体
     * @return 技能工具关联数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    AiSkillToolDo toDo(SkillToolEntity skillToolEntity);
}
