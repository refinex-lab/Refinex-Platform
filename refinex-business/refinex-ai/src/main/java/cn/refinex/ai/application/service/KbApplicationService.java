package cn.refinex.ai.application.service;

import cn.refinex.ai.application.assembler.KbDomainAssembler;
import cn.refinex.ai.application.command.*;
import cn.refinex.ai.application.dto.DocumentDTO;
import cn.refinex.ai.application.dto.FolderDTO;
import cn.refinex.ai.application.dto.KnowledgeBaseDTO;
import cn.refinex.ai.domain.error.AiErrorCode;
import cn.refinex.ai.domain.model.entity.DocumentChunkEntity;
import cn.refinex.ai.domain.model.entity.DocumentEntity;
import cn.refinex.ai.domain.model.entity.FolderEntity;
import cn.refinex.ai.domain.model.entity.KnowledgeBaseEntity;
import cn.refinex.ai.domain.repository.AiRepository;
import cn.refinex.ai.infrastructure.ai.VectorStoreRouter;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.PageResponse;
import cn.refinex.base.utils.PageUtils;
import cn.refinex.satoken.helper.LoginUserHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.getIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * 知识库应用服务
 *
 * @author refinex
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KbApplicationService {

    private final AiRepository aiRepository;
    private final KbDomainAssembler kbDomainAssembler;
    private final VectorStoreRouter vectorStoreRouter;
    private final VectorizationService vectorizationService;

    // ══════════════════════════════════════
    // KnowledgeBase（知识库）
    // ══════════════════════════════════════

    /**
     * 查询知识库分页列表
     *
     * @param command 查询知识库列表命令
     * @return 知识库分页列表
     */
    public PageResponse<KnowledgeBaseDTO> listKnowledgeBases(QueryKnowledgeBaseListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);

        Long estabId = LoginUserHelper.getEstabId();

        PageResponse<KnowledgeBaseEntity> entities = aiRepository.listKnowledgeBases(
                estabId,
                command == null ? null : command.getStatus(),
                command == null ? null : command.getKeyword(),
                currentPage, pageSize
        );

        List<KnowledgeBaseDTO> result = new ArrayList<>();
        for (KnowledgeBaseEntity entity : entities.getData()) {
            result.add(kbDomainAssembler.toKnowledgeBaseDto(entity));
        }

        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询全部知识库（不分页，用于下拉选择）
     *
     * @param status 状态（0-禁用，1-启用）
     * @return 知识库列表
     */
    public List<KnowledgeBaseDTO> listAllKnowledgeBases(Integer status) {
        Long estabId = LoginUserHelper.getEstabId();
        List<KnowledgeBaseEntity> entities = aiRepository.listAllKnowledgeBases(estabId, status);
        List<KnowledgeBaseDTO> result = new ArrayList<>();
        for (KnowledgeBaseEntity entity : entities) {
            result.add(kbDomainAssembler.toKnowledgeBaseDto(entity));
        }
        return result;
    }

    /**
     * 查询知识库详情
     *
     * @param id 知识库ID
     * @return 知识库详情
     */
    public KnowledgeBaseDTO getKnowledgeBase(Long id) {
        KnowledgeBaseEntity entity = requireKnowledgeBase(id);
        return kbDomainAssembler.toKnowledgeBaseDto(entity);
    }

    /**
     * 创建知识库
     *
     * @param command 创建知识库命令
     * @return 创建的知识库DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeBaseDTO createKnowledgeBase(CreateKnowledgeBaseCommand command) {
        if (command == null || isBlank(command.getKbCode()) || isBlank(command.getKbName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        Long estabId = LoginUserHelper.getEstabId();
        String kbCode = command.getKbCode().trim();

        if (aiRepository.countKbCode(estabId, kbCode, null) > 0) {
            throw new BizException(AiErrorCode.KB_CODE_DUPLICATED);
        }

        KnowledgeBaseEntity entity = new KnowledgeBaseEntity();
        entity.setEstabId(estabId);
        entity.setKbCode(kbCode);
        entity.setKbName(command.getKbName().trim());
        entity.setDescription(trimToNull(command.getDescription()));
        entity.setIcon(trimToNull(command.getIcon()));
        entity.setVisibility(getIfNull(command.getVisibility(), 0));
        entity.setVectorized(getIfNull(command.getVectorized(), 0));
        entity.setEmbeddingModelId(command.getEmbeddingModelId());
        entity.setChunkSize(command.getChunkSize());
        entity.setChunkOverlap(command.getChunkOverlap());
        entity.setDocCount(0);
        entity.setStatus(getIfNull(command.getStatus(), 1));
        entity.setSort(getIfNull(command.getSort(), 0));
        entity.setRemark(trimToNull(command.getRemark()));
        entity.setExtJson(trimToNull(command.getExtJson()));

        KnowledgeBaseEntity created = aiRepository.insertKnowledgeBase(entity);
        return kbDomainAssembler.toKnowledgeBaseDto(created);
    }

    /**
     * 更新知识库
     *
     * @param command 更新知识库命令
     * @return 更新后的知识库DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeBaseDTO updateKnowledgeBase(UpdateKnowledgeBaseCommand command) {
        if (command == null || command.getKbId() == null || isBlank(command.getKbName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        KnowledgeBaseEntity existing = requireKnowledgeBase(command.getKbId());
        existing.setKbName(command.getKbName().trim());
        existing.setDescription(trimToNull(command.getDescription()));
        existing.setIcon(trimToNull(command.getIcon()));
        existing.setVisibility(getIfNull(command.getVisibility(), existing.getVisibility()));
        existing.setVectorized(getIfNull(command.getVectorized(), existing.getVectorized()));
        existing.setEmbeddingModelId(command.getEmbeddingModelId());
        existing.setChunkSize(command.getChunkSize());
        existing.setChunkOverlap(command.getChunkOverlap());
        existing.setStatus(getIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(getIfNull(command.getSort(), existing.getSort()));
        existing.setRemark(trimToNull(command.getRemark()));
        existing.setExtJson(trimToNull(command.getExtJson()));

        aiRepository.updateKnowledgeBase(existing);
        return kbDomainAssembler.toKnowledgeBaseDto(requireKnowledgeBase(existing.getId()));
    }

    /**
     * 删除知识库（逻辑删除，校验无文档才可删）
     *
     * @param id 知识库ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeBase(Long id) {
        requireKnowledgeBase(id);
        if (aiRepository.countDocumentsByKnowledgeBaseId(id) > 0) {
            throw new BizException(AiErrorCode.KB_HAS_DOCUMENTS);
        }
        aiRepository.deleteKnowledgeBaseById(id);
    }

    // ══════════════════════════════════════
    // Folder（目录）
    // ══════════════════════════════════════

    /**
     * 查询知识库下的全部目录（前端构建树）
     *
     * @param knowledgeBaseId 知识库ID
     * @return 目录列表
     */
    public List<FolderDTO> listFolders(Long knowledgeBaseId) {
        requireKnowledgeBase(knowledgeBaseId);
        List<FolderEntity> entities = aiRepository.listFoldersByKnowledgeBaseId(knowledgeBaseId);
        List<FolderDTO> result = new ArrayList<>();
        for (FolderEntity entity : entities) {
            result.add(kbDomainAssembler.toFolderDto(entity));
        }
        return result;
    }

    /**
     * 创建目录
     *
     * @param command 创建目录命令
     * @return 创建的目录DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public FolderDTO createFolder(CreateFolderCommand command) {
        if (command == null || command.getKnowledgeBaseId() == null || isBlank(command.getFolderName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        requireKnowledgeBase(command.getKnowledgeBaseId());

        Long parentId = getIfNull(command.getParentId(), 0L);
        String folderName = command.getFolderName().trim();

        if (aiRepository.countFolderName(command.getKnowledgeBaseId(), parentId, folderName, null) > 0) {
            throw new BizException(AiErrorCode.KB_FOLDER_NAME_DUPLICATED);
        }

        FolderEntity entity = new FolderEntity();
        entity.setKnowledgeBaseId(command.getKnowledgeBaseId());
        entity.setParentId(parentId);
        entity.setFolderName(folderName);
        entity.setSort(getIfNull(command.getSort(), 0));
        entity.setRemark(trimToNull(command.getRemark()));
        entity.setExtJson(trimToNull(command.getExtJson()));

        FolderEntity created = aiRepository.insertFolder(entity);
        return kbDomainAssembler.toFolderDto(created);
    }

    /**
     * 更新目录
     *
     * @param command 更新目录命令
     * @return 更新后的目录DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public FolderDTO updateFolder(UpdateFolderCommand command) {
        if (command == null || command.getFolderId() == null || isBlank(command.getFolderName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        FolderEntity existing = requireFolder(command.getFolderId());
        String folderName = command.getFolderName().trim();

        if (aiRepository.countFolderName(existing.getKnowledgeBaseId(), existing.getParentId(), folderName, existing.getId()) > 0) {
            throw new BizException(AiErrorCode.KB_FOLDER_NAME_DUPLICATED);
        }

        existing.setFolderName(folderName);
        existing.setExtJson(trimToNull(command.getExtJson()));

        aiRepository.updateFolder(existing);
        return kbDomainAssembler.toFolderDto(requireFolder(existing.getId()));
    }

    /**
     * 删除目录（校验无子目录和文档才可删）
     *
     * @param id 目录ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFolder(Long id) {
        requireFolder(id);
        if (aiRepository.countFoldersByParentId(id) > 0) {
            throw new BizException(AiErrorCode.KB_FOLDER_HAS_CHILDREN);
        }
        if (aiRepository.countDocumentsByFolderId(id) > 0) {
            throw new BizException(AiErrorCode.KB_FOLDER_HAS_CHILDREN);
        }
        aiRepository.deleteFolderById(id);
    }

    // ══════════════════════════════════════
    // Document（文档）
    // ══════════════════════════════════════

    /**
     * 查询文档分页列表
     *
     * @param command 查询文档列表命令
     * @return 文档分页列表
     */
    public PageResponse<DocumentDTO> listDocuments(QueryDocumentListCommand command) {
        if (command == null || command.getKnowledgeBaseId() == null) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        int currentPage = PageUtils.normalizeCurrentPage(command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);

        PageResponse<DocumentEntity> entities = aiRepository.listDocuments(
                command.getKnowledgeBaseId(),
                command.getFolderId(),
                command.getStatus(),
                command.getKeyword(),
                currentPage, pageSize
        );

        List<DocumentDTO> result = new ArrayList<>();
        for (DocumentEntity entity : entities.getData()) {
            result.add(kbDomainAssembler.toDocumentDto(entity));
        }

        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询文档详情
     *
     * @param id 文档ID
     * @return 文档详情
     */
    public DocumentDTO getDocument(Long id) {
        DocumentEntity entity = requireDocument(id);
        return kbDomainAssembler.toDocumentDto(entity);
    }

    /**
     * 获取文档内容（大文本单独接口）
     *
     * @param id 文档ID
     * @return 文档纯文本内容
     */
    public String getDocumentContent(Long id) {
        DocumentEntity entity = requireDocument(id);
        return entity.getContent();
    }

    /**
     * 创建文档
     *
     * @param command 创建文档命令
     * @return 创建的文档DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public DocumentDTO createDocument(CreateDocumentCommand command) {
        if (command == null || command.getKnowledgeBaseId() == null || isBlank(command.getDocName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        requireKnowledgeBase(command.getKnowledgeBaseId());

        Long folderId = getIfNull(command.getFolderId(), 0L);
        String docName = command.getDocName().trim();

        if (aiRepository.countDocumentName(command.getKnowledgeBaseId(), folderId, docName, null) > 0) {
            throw new BizException(AiErrorCode.KB_DOCUMENT_NAME_DUPLICATED);
        }

        DocumentEntity entity = new DocumentEntity();
        entity.setKnowledgeBaseId(command.getKnowledgeBaseId());
        entity.setFolderId(folderId);
        entity.setDocName(docName);
        entity.setDocType(trimToNull(command.getDocType()));
        entity.setFileUrl(trimToNull(command.getFileUrl()));
        entity.setFileSize(command.getFileSize());
        entity.setVectorStatus(0);
        entity.setChunkCount(0);
        entity.setStatus(getIfNull(command.getStatus(), 1));
        entity.setSort(getIfNull(command.getSort(), 0));
        entity.setRemark(trimToNull(command.getRemark()));
        entity.setExtJson(trimToNull(command.getExtJson()));

        // Tika 内容提取
        extractDocumentContent(entity);

        DocumentEntity created = aiRepository.insertDocument(entity);

        // 更新知识库 docCount
        updateKbDocCount(command.getKnowledgeBaseId());

        // 自动向量化：知识库开启向量化且文档内容非空时，异步触发向量化
        KnowledgeBaseEntity kb = aiRepository.findKnowledgeBaseById(command.getKnowledgeBaseId());
        if (kb != null && kb.getVectorized() != null && kb.getVectorized() == 1 && created.getContent() != null && !created.getContent().isBlank()) {
            try {
                vectorizationService.vectorizeDocument(kb.getId(), created.getId());
            } catch (Exception e) {
                log.warn("文档创建后自动向量化触发失败: kbId={}, docId={}", kb.getId(), created.getId(), e);
            }
        }

        return kbDomainAssembler.toDocumentDto(created);
    }

    /**
     * 更新文档
     *
     * @param command 更新文档命令
     * @return 更新后的文档DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public DocumentDTO updateDocument(UpdateDocumentCommand command) {
        if (command == null || command.getDocumentId() == null || isBlank(command.getDocName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        DocumentEntity existing = requireDocument(command.getDocumentId());
        String docName = command.getDocName().trim();
        Long folderId = getIfNull(command.getFolderId(), existing.getFolderId());

        if (aiRepository.countDocumentName(existing.getKnowledgeBaseId(), folderId, docName, existing.getId()) > 0) {
            throw new BizException(AiErrorCode.KB_DOCUMENT_NAME_DUPLICATED);
        }

        existing.setDocName(docName);
        existing.setFolderId(folderId);
        existing.setStatus(getIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(getIfNull(command.getSort(), existing.getSort()));
        existing.setRemark(trimToNull(command.getRemark()));
        existing.setExtJson(trimToNull(command.getExtJson()));

        aiRepository.updateDocument(existing);
        return kbDomainAssembler.toDocumentDto(requireDocument(existing.getId()));
    }

    /**
     * 删除文档（级联删除切片，更新 docCount）
     *
     * @param id 文档ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(Long id) {
        DocumentEntity entity = requireDocument(id);

        // 清理向量库中的向量数据（仅在 KB 开启向量化时执行）
        KnowledgeBaseEntity kb = aiRepository.findKnowledgeBaseById(entity.getKnowledgeBaseId());
        if (kb != null && kb.getVectorized() != null && kb.getVectorized() == 1) {
            List<DocumentChunkEntity> chunks = aiRepository.listChunksByDocumentId(id);
            List<String> embeddingIds = chunks.stream()
                    .map(DocumentChunkEntity::getEmbeddingId)
                    .filter(eid -> eid != null && !eid.isBlank())
                    .toList();

            if (!embeddingIds.isEmpty()) {
                try {
                    VectorStore vectorStore = vectorStoreRouter.resolve(kb);
                    vectorStore.delete(embeddingIds);
                } catch (Exception e) {
                    log.warn("删除文档时清理向量库失败: docId={}, error={}", id, e.getMessage());
                }
            }
        }

        aiRepository.deleteChunksByDocumentId(id);
        aiRepository.deleteDocumentById(id);
        updateKbDocCount(entity.getKnowledgeBaseId());
    }

    // ══════════════════════════════════════
    // 拖拽排序
    // ══════════════════════════════════════

    /**
     * 批量更新目录/文档排序
     *
     * @param knowledgeBaseId 知识库ID
     * @param items           排序项列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void sortItems(Long knowledgeBaseId, List<SortItemCommand> items) {
        requireKnowledgeBase(knowledgeBaseId);
        if (items == null || items.isEmpty()) {
            return;
        }
        for (SortItemCommand item : items) {
            if ("FOLDER".equalsIgnoreCase(item.getType())) {
                aiRepository.updateFolderSort(item.getId(), item.getSort());
            } else if ("DOCUMENT".equalsIgnoreCase(item.getType())) {
                aiRepository.updateDocumentSort(item.getId(), item.getSort());
            }
        }
    }

    // ══════════════════════════════════════
    // 内部辅助方法
    // ══════════════════════════════════════

    /**
     * 校验并返回知识库实体
     *
     * @param id 知识库ID
     * @return 知识库实体
     */
    private KnowledgeBaseEntity requireKnowledgeBase(Long id) {
        if (id == null) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        KnowledgeBaseEntity entity = aiRepository.findKnowledgeBaseById(id);
        if (entity == null || (entity.getDeleted() != null && entity.getDeleted() == 1)) {
            throw new BizException(AiErrorCode.KB_NOT_FOUND);
        }

        return entity;
    }

    /**
     * 校验并返回目录实体
     *
     * @param id 目录ID
     * @return 目录实体
     */
    private FolderEntity requireFolder(Long id) {
        if (id == null) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        FolderEntity entity = aiRepository.findFolderById(id);
        if (entity == null || (entity.getDeleted() != null && entity.getDeleted() == 1)) {
            throw new BizException(AiErrorCode.KB_FOLDER_NOT_FOUND);
        }

        return entity;
    }

    /**
     * 校验并返回文档实体
     *
     * @param id 文档ID
     * @return 文档实体
     */
    private DocumentEntity requireDocument(Long id) {
        if (id == null) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        DocumentEntity entity = aiRepository.findDocumentById(id);
        if (entity == null || (entity.getDeleted() != null && entity.getDeleted() == 1)) {
            throw new BizException(AiErrorCode.KB_DOCUMENT_NOT_FOUND);
        }

        return entity;
    }

    /**
     * 更新知识库文档数量冗余计数
     *
     * @param knowledgeBaseId 知识库ID
     */
    private void updateKbDocCount(Long knowledgeBaseId) {
        long docCount = aiRepository.countDocumentsByKnowledgeBaseId(knowledgeBaseId);
        KnowledgeBaseEntity kb = aiRepository.findKnowledgeBaseById(knowledgeBaseId);
        if (kb != null) {
            kb.setDocCount((int) docCount);
            aiRepository.updateKnowledgeBase(kb);
        }
    }

    /**
     * 通过 Tika 提取文档内容，写入 content/charCount/tokenCount
     *
     * <p>流程：fileUrl → UrlResource → TikaDocumentReader → 拼接纯文本 → 计算字符数和估算 token 数</p>
     *
     * @param entity 待填充内容的文档实体（需已设置 fileUrl）
     */
    private void extractDocumentContent(DocumentEntity entity) {
        String fileUrl = entity.getFileUrl();
        if (isBlank(fileUrl)) {
            return;
        }

        try {
            UrlResource resource = new UrlResource(URI.create(fileUrl));
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            List<Document> documents = reader.read();

            String content = documents.stream()
                    .map(Document::getText)
                    .filter(text -> text != null && !text.isBlank())
                    .collect(Collectors.joining("\n\n"));

            entity.setContent(content);
            entity.setCharCount(content.length());
            entity.setTokenCount(estimateTokenCount(content));
        } catch (Exception e) {
            log.error("文档内容提取失败, fileUrl={}", fileUrl, e);
            throw new BizException(AiErrorCode.KB_DOCUMENT_PARSE_FAILED);
        }
    }

    /**
     * 估算文本的 token 数量
     *
     * <p>简单估算策略：中文按 1 字符 ≈ 1 token，英文/数字按 4 字符 ≈ 1 token</p>
     *
     * @param text 纯文本内容
     * @return 估算的 token 数
     */
    private int estimateTokenCount(String text) {
        if (isBlank(text)) {
            return 0;
        }
        int cjkCount = 0;
        int otherCount = 0;
        for (char c : text.toCharArray()) {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN
                    || Character.UnicodeScript.of(c) == Character.UnicodeScript.HIRAGANA
                    || Character.UnicodeScript.of(c) == Character.UnicodeScript.KATAKANA
                    || Character.UnicodeScript.of(c) == Character.UnicodeScript.HANGUL) {
                cjkCount++;
            } else {
                otherCount++;
            }
        }
        return cjkCount + (otherCount / 4);
    }
}
