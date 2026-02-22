package cn.refinex.ai.interfaces.assembler;

import cn.refinex.ai.application.command.*;
import cn.refinex.ai.application.dto.DocumentDTO;
import cn.refinex.ai.application.dto.FolderDTO;
import cn.refinex.ai.application.dto.KnowledgeBaseDTO;
import cn.refinex.ai.interfaces.dto.*;
import cn.refinex.ai.interfaces.vo.DocumentVO;
import cn.refinex.ai.interfaces.vo.FolderVO;
import cn.refinex.ai.interfaces.vo.KnowledgeBaseVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * KB 接口层组装器（Request/Query ↔ Command，DTO → VO）
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface KbApiAssembler {

    // ── KnowledgeBase ──

    /**
     * 查询知识库列表参数转换
     *
     * @param query 查询知识库列表参数
     * @return 查询知识库列表命令
     */
    QueryKnowledgeBaseListCommand toQueryKnowledgeBaseListCommand(KnowledgeBaseListQuery query);

    /**
     * 创建知识库参数转换
     *
     * @param request 创建知识库参数
     * @return 创建知识库命令
     */
    CreateKnowledgeBaseCommand toCreateKnowledgeBaseCommand(KnowledgeBaseCreateRequest request);

    /**
     * 更新知识库参数转换
     *
     * @param request 更新知识库参数
     * @return 更新知识库命令
     */
    @Mapping(target = "kbId", ignore = true)
    UpdateKnowledgeBaseCommand toUpdateKnowledgeBaseCommand(KnowledgeBaseUpdateRequest request);

    /**
     * 知识库DTO转换为知识库VO
     *
     * @param dto 知识库DTO
     * @return 知识库VO
     */
    KnowledgeBaseVO toKnowledgeBaseVo(KnowledgeBaseDTO dto);

    /**
     * 知识库DTO列表转换为知识库VO列表
     *
     * @param dtos 知识库DTO列表
     * @return 知识库VO列表
     */
    List<KnowledgeBaseVO> toKnowledgeBaseVoList(List<KnowledgeBaseDTO> dtos);

    // ── Folder ──

    /**
     * 创建目录参数转换
     *
     * @param request 创建目录参数
     * @return 创建目录命令
     */
    @Mapping(target = "knowledgeBaseId", ignore = true)
    CreateFolderCommand toCreateFolderCommand(FolderCreateRequest request);

    /**
     * 更新目录参数转换
     *
     * @param request 更新目录参数
     * @return 更新目录命令
     */
    @Mapping(target = "folderId", ignore = true)
    UpdateFolderCommand toUpdateFolderCommand(FolderUpdateRequest request);

    /**
     * 目录DTO转换为目录VO
     *
     * @param dto 目录DTO
     * @return 目录VO
     */
    FolderVO toFolderVo(FolderDTO dto);

    /**
     * 目录DTO列表转换为目录VO列表
     *
     * @param dtos 目录DTO列表
     * @return 目录VO列表
     */
    List<FolderVO> toFolderVoList(List<FolderDTO> dtos);

    // ── Document ──

    /**
     * 查询文档列表参数转换
     *
     * @param query 查询文档列表参数
     * @return 查询文档列表命令
     */
    @Mapping(target = "knowledgeBaseId", ignore = true)
    QueryDocumentListCommand toQueryDocumentListCommand(DocumentListQuery query);

    /**
     * 创建文档参数转换
     *
     * @param request 创建文档参数
     * @return 创建文档命令
     */
    @Mapping(target = "knowledgeBaseId", ignore = true)
    CreateDocumentCommand toCreateDocumentCommand(DocumentCreateRequest request);

    /**
     * 更新文档参数转换
     *
     * @param request 更新文档参数
     * @return 更新文档命令
     */
    @Mapping(target = "documentId", ignore = true)
    UpdateDocumentCommand toUpdateDocumentCommand(DocumentUpdateRequest request);

    /**
     * 文档DTO转换为文档VO
     *
     * @param dto 文档DTO
     * @return 文档VO
     */
    DocumentVO toDocumentVo(DocumentDTO dto);

    /**
     * 文档DTO列表转换为文档VO列表
     *
     * @param dtos 文档DTO列表
     * @return 文档VO列表
     */
    List<DocumentVO> toDocumentVoList(List<DocumentDTO> dtos);

    // ── Sort ──

    /**
     * 排序请求项转换为排序命令
     *
     * @param item 排序请求项
     * @return 排序命令
     */
    SortItemCommand toSortItemCommand(SortRequest.SortItem item);

    /**
     * 排序请求项列表转换为排序命令列表
     *
     * @param items 排序请求项列表
     * @return 排序命令列表
     */
    List<SortItemCommand> toSortItemCommandList(List<SortRequest.SortItem> items);
}
