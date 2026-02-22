package cn.refinex.ai.application.assembler;

import cn.refinex.ai.application.dto.DocumentDTO;
import cn.refinex.ai.application.dto.FolderDTO;
import cn.refinex.ai.application.dto.KnowledgeBaseDTO;
import cn.refinex.ai.domain.model.entity.DocumentEntity;
import cn.refinex.ai.domain.model.entity.FolderEntity;
import cn.refinex.ai.domain.model.entity.KnowledgeBaseEntity;
import org.mapstruct.Mapper;

/**
 * KB 领域层组装器（Entity → DTO）
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface KbDomainAssembler {

    /**
     * 将知识库实体转换为知识库 DTO
     *
     * @param entity 知识库实体
     * @return 知识库 DTO
     */
    KnowledgeBaseDTO toKnowledgeBaseDto(KnowledgeBaseEntity entity);

    /**
     * 将目录实体转换为目录 DTO
     *
     * @param entity 目录实体
     * @return 目录 DTO
     */
    FolderDTO toFolderDto(FolderEntity entity);

    /**
     * 将文档实体转换为文档 DTO（不含 content 大文本）
     *
     * @param entity 文档实体
     * @return 文档 DTO
     */
    DocumentDTO toDocumentDto(DocumentEntity entity);
}
