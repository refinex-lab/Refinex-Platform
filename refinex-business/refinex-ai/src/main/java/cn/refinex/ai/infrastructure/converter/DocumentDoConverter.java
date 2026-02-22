package cn.refinex.ai.infrastructure.converter;

import cn.refinex.ai.domain.model.entity.DocumentEntity;
import cn.refinex.ai.infrastructure.persistence.dataobject.KbDocumentDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 知识库文档 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface DocumentDoConverter {

    /**
     * 转换为文档实体
     *
     * @param documentDo 文档数据对象
     * @return 文档实体
     */
    DocumentEntity toEntity(KbDocumentDo documentDo);

    /**
     * 转换为文档数据对象
     *
     * @param documentEntity 文档实体
     * @return 文档数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    KbDocumentDo toDo(DocumentEntity documentEntity);
}
