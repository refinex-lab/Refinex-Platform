package cn.refinex.ai.infrastructure.converter;

import cn.refinex.ai.domain.model.entity.ConversationEntity;
import cn.refinex.ai.infrastructure.persistence.dataobject.AiConversationDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 对话 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface ConversationDoConverter {

    /**
     * 转换为对话实体
     *
     * @param conversationDo 对话数据对象
     * @return 对话实体
     */
    ConversationEntity toEntity(AiConversationDo conversationDo);

    /**
     * 转换为对话数据对象
     *
     * @param conversationEntity 对话实体
     * @return 对话数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    AiConversationDo toDo(ConversationEntity conversationEntity);
}
