package cn.refinex.ai.infrastructure.converter;

import cn.refinex.ai.domain.model.entity.FolderEntity;
import cn.refinex.ai.infrastructure.persistence.dataobject.KbFolderDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 知识库目录 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface FolderDoConverter {

    /**
     * 转换为目录实体
     *
     * @param folderDo 目录数据对象
     * @return 目录实体
     */
    FolderEntity toEntity(KbFolderDo folderDo);

    /**
     * 转换为目录数据对象
     *
     * @param folderEntity 目录实体
     * @return 目录数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    KbFolderDo toDo(FolderEntity folderEntity);
}
