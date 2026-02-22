package cn.refinex.ai.infrastructure.converter;

import cn.refinex.ai.domain.model.entity.DocumentChunkEntity;
import cn.refinex.ai.infrastructure.persistence.dataobject.KbDocumentChunkDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 文档切片 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface DocumentChunkDoConverter {

    /**
     * 转换为文档切片实体
     *
     * @param documentChunkDo 文档切片数据对象
     * @return 文档切片实体
     */
    DocumentChunkEntity toEntity(KbDocumentChunkDo documentChunkDo);

    /**
     * 转换为文档切片数据对象
     *
     * @param documentChunkEntity 文档切片实体
     * @return 文档切片数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    KbDocumentChunkDo toDo(DocumentChunkEntity documentChunkEntity);
}
