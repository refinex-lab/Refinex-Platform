package cn.refinex.ai.application.service;

import cn.refinex.ai.application.assembler.KbDomainAssembler;
import cn.refinex.ai.application.dto.DocumentChunkDTO;
import cn.refinex.ai.application.dto.SearchResultDTO;
import cn.refinex.ai.domain.error.AiErrorCode;
import cn.refinex.ai.domain.model.entity.DocumentChunkEntity;
import cn.refinex.ai.domain.model.entity.DocumentEntity;
import cn.refinex.ai.domain.model.entity.KnowledgeBaseEntity;
import cn.refinex.ai.domain.model.enums.VectorStatus;
import cn.refinex.ai.domain.repository.AiRepository;
import cn.refinex.ai.infrastructure.ai.VectorStoreRouter;
import cn.refinex.base.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 向量化应用服务
 * <p>
 * 负责文档切片、嵌入向量化、向量存储、相似度检索等 RAG 核心能力。
 *
 * @author refinex
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorizationService {

    private final AiRepository aiRepository;
    private final VectorStoreRouter vectorStoreRouter;
    private final KbDomainAssembler kbDomainAssembler;

    /**
     * 单文档异步向量化
     * <p>
     * 立即将文档状态设为 VECTORIZING 并返回，实际向量化在 boundedElastic 线程池异步执行。
     *
     * @param kbId       知识库ID
     * @param documentId 文档ID
     */
    public void vectorizeDocument(Long kbId, Long documentId) {
        KnowledgeBaseEntity kb = requireVectorizedKb(kbId);
        DocumentEntity doc = requireDocument(documentId);

        if (doc.getVectorStatus() != null && doc.getVectorStatus() == VectorStatus.VECTORIZING.getCode()) {
            throw new BizException(AiErrorCode.VECTORIZATION_IN_PROGRESS);
        }

        // 立即标记为向量化中
        doc.setVectorStatus(VectorStatus.VECTORIZING.getCode());
        doc.setVectorError(null);
        aiRepository.updateDocument(doc);

        // 异步执行向量化
        Mono.fromRunnable(() -> doVectorize(kb, doc))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        unused -> {},
                        error -> log.error("文档向量化异步执行异常: kbId={}, docId={}", kbId, documentId, error)
                );
    }

    /**
     * 批量异步向量化知识库下所有待向量化文档
     *
     * @param kbId 知识库ID
     */
    public void vectorizeKnowledgeBase(Long kbId) {
        KnowledgeBaseEntity kb = requireVectorizedKb(kbId);
        List<DocumentEntity> docs = aiRepository.listDocumentsForVectorization(kbId);

        if (docs.isEmpty()) {
            return;
        }

        // 批量标记为向量化中
        for (DocumentEntity doc : docs) {
            doc.setVectorStatus(VectorStatus.VECTORIZING.getCode());
            doc.setVectorError(null);
            aiRepository.updateDocument(doc);
        }

        // 异步逐个向量化
        Mono.fromRunnable(() -> {
            for (DocumentEntity doc : docs) {
                try {
                    doVectorize(kb, doc);
                } catch (Exception e) {
                    log.error("批量向量化中单文档失败: kbId={}, docId={}", kbId, doc.getId(), e);
                }
            }
        }).subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        unused -> {},
                        error -> log.error("批量向量化异步执行异常: kbId={}", kbId, error)
                );
    }

    /**
     * 移除文档向量（从向量库和 MySQL 切片表中清理）
     *
     * @param kbId       知识库ID
     * @param documentId 文档ID
     */
    public void devectorizeDocument(Long kbId, Long documentId) {
        KnowledgeBaseEntity kb = requireVectorizedKb(kbId);
        DocumentEntity doc = requireDocument(documentId);

        cleanExistingVectors(kb, documentId);

        doc.setVectorStatus(VectorStatus.NOT_VECTORIZED.getCode());
        doc.setVectorError(null);
        doc.setChunkCount(0);
        doc.setLastVectorizedAt(null);
        aiRepository.updateDocument(doc);
    }

    /**
     * 查看文档切片列表
     *
     * @param kbId       知识库ID
     * @param documentId 文档ID
     * @return 切片 DTO 列表
     */
    public List<DocumentChunkDTO> listChunks(Long kbId, Long documentId) {
        requireVectorizedKb(kbId);
        DocumentEntity doc = requireDocument(documentId);
        if (!doc.getKnowledgeBaseId().equals(kbId)) {
            throw new BizException(AiErrorCode.KB_DOCUMENT_NOT_FOUND);
        }
        List<DocumentChunkEntity> chunks = aiRepository.listChunksByDocumentId(documentId);
        List<DocumentChunkDTO> result = new ArrayList<>();
        for (DocumentChunkEntity chunk : chunks) {
            result.add(kbDomainAssembler.toDocumentChunkDto(chunk));
        }
        return result;
    }

    /**
     * 测试 RAG 检索
     *
     * @param kbId               知识库ID
     * @param query              查询文本
     * @param topK               返回结果数量
     * @param similarityThreshold 相似度阈值
     * @return 检索结果列表
     */
    public List<SearchResultDTO> search(Long kbId, String query, Integer topK, Double similarityThreshold) {
        KnowledgeBaseEntity kb = requireVectorizedKb(kbId);
        VectorStore vectorStore = vectorStoreRouter.resolve(kb);

        int k = (topK != null && topK > 0) ? topK : 5;
        double threshold = (similarityThreshold != null) ? similarityThreshold : 0.0;

        FilterExpressionBuilder b = new FilterExpressionBuilder();
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(k)
                .similarityThreshold(threshold)
                .filterExpression(b.eq("knowledge_base_id", String.valueOf(kbId)).build())
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);

        List<SearchResultDTO> dtos = new ArrayList<>();
        for (Document doc : results) {
            SearchResultDTO dto = new SearchResultDTO();
            dto.setContent(doc.getText());
            dto.setScore(doc.getScore());
            dto.setMetadata(doc.getMetadata());
            dtos.add(dto);
        }
        return dtos;
    }

    // ══════════════════════════════════════
    // 内部方法
    // ══════════════════════════════════════

    /**
     * 执行单文档向量化核心流程
     *
     * @param kb  知识库实体
     * @param doc 文档实体
     */
    private void doVectorize(KnowledgeBaseEntity kb, DocumentEntity doc) {
        try {
            // 1. 清理已有向量和切片
            cleanExistingVectors(kb, doc.getId());

            // 2. 文本切片
            String content = doc.getContent();
            if (content == null || content.isBlank()) {
                doc.setVectorStatus(VectorStatus.FAILED.getCode());
                doc.setVectorError("文档内容为空");
                aiRepository.updateDocument(doc);
                return;
            }

            int chunkSize = (kb.getChunkSize() != null && kb.getChunkSize() > 0) ? kb.getChunkSize() : 512;
            int chunkOverlap = (kb.getChunkOverlap() != null && kb.getChunkOverlap() > 0) ? kb.getChunkOverlap() : 64;

            TokenTextSplitter splitter = new TokenTextSplitter(
                    chunkSize,                              // defaultChunkSize
                    Math.max(chunkOverlap, 50),             // minChunkSizeChars
                    5,                                      // minChunkLengthToEmbed
                    10000,                                  // maxNumChunks
                    true                                    // keepSeparator
            );

            // 构建 Spring AI Document 用于切片
            Document sourceDoc = new Document(content);
            List<Document> splitDocs = splitter.split(sourceDoc);

            if (splitDocs.isEmpty()) {
                doc.setVectorStatus(VectorStatus.FAILED.getCode());
                doc.setVectorError("文档切片结果为空");
                aiRepository.updateDocument(doc);
                return;
            }

            // 3. 为每个切片注入 metadata
            List<DocumentChunkEntity> chunkEntities = new ArrayList<>();
            for (int i = 0; i < splitDocs.size(); i++) {
                Document splitDoc = splitDocs.get(i);
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("knowledge_base_id", String.valueOf(kb.getId()));
                metadata.put("document_id", String.valueOf(doc.getId()));
                metadata.put("estab_id", String.valueOf(kb.getEstabId()));
                metadata.put("chunk_index", i);
                metadata.put("doc_name", doc.getDocName());
                splitDoc.getMetadata().putAll(metadata);

                // 构建切片实体
                DocumentChunkEntity chunk = new DocumentChunkEntity();
                chunk.setDocumentId(doc.getId());
                chunk.setKnowledgeBaseId(kb.getId());
                chunk.setChunkIndex(i);
                chunk.setContent(splitDoc.getText());
                chunk.setTokenCount(estimateTokenCount(splitDoc.getText()));
                chunk.setEmbeddingId(splitDoc.getId());
                chunk.setMetadata(null);
                chunkEntities.add(chunk);
            }

            // 4. VectorStore.add() — 内部自动调用 EmbeddingModel.embed() 并存储
            VectorStore vectorStore = vectorStoreRouter.resolve(kb);
            vectorStore.add(splitDocs);

            // 5. 回写 embeddingId（Spring AI Document.id 即向量 ID）
            for (int i = 0; i < splitDocs.size(); i++) {
                chunkEntities.get(i).setEmbeddingId(splitDocs.get(i).getId());
            }

            // 6. 持久化切片到 MySQL
            aiRepository.batchInsertChunks(chunkEntities);

            // 7. 更新文档状态
            doc.setVectorStatus(VectorStatus.COMPLETED.getCode());
            doc.setVectorError(null);
            doc.setChunkCount(chunkEntities.size());
            doc.setLastVectorizedAt(LocalDateTime.now());
            aiRepository.updateDocument(doc);

            log.info("文档向量化完成: kbId={}, docId={}, chunks={}", kb.getId(), doc.getId(), chunkEntities.size());

        } catch (Exception e) {
            log.error("文档向量化失败: kbId={}, docId={}", kb.getId(), doc.getId(), e);
            doc.setVectorStatus(VectorStatus.FAILED.getCode());
            doc.setVectorError(e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500)) : "未知错误");
            try {
                aiRepository.updateDocument(doc);
            } catch (Exception updateEx) {
                log.error("更新文档向量化失败状态异常: docId={}", doc.getId(), updateEx);
            }
        }
    }

    /**
     * 清理文档已有的向量和切片
     *
     * @param kb         知识库实体
     * @param documentId 文档ID
     */
    private void cleanExistingVectors(KnowledgeBaseEntity kb, Long documentId) {
        List<DocumentChunkEntity> existingChunks = aiRepository.listChunksByDocumentId(documentId);
        if (!existingChunks.isEmpty()) {
            // 收集向量 ID，从向量库删除
            List<String> embeddingIds = existingChunks.stream()
                    .map(DocumentChunkEntity::getEmbeddingId)
                    .filter(id -> id != null && !id.isBlank())
                    .collect(Collectors.toList());

            if (!embeddingIds.isEmpty()) {
                try {
                    VectorStore vectorStore = vectorStoreRouter.resolve(kb);
                    vectorStore.delete(embeddingIds);
                } catch (Exception e) {
                    log.warn("清理向量库失败（可能向量库未初始化）: docId={}, error={}", documentId, e.getMessage());
                }
            }

            // 删除 MySQL 切片
            aiRepository.deleteChunksByDocumentId(documentId);
        }
    }

    /**
     * 校验并返回已开启向量化的知识库
     *
     * @param kbId 知识库ID
     * @return 知识库实体
     */
    private KnowledgeBaseEntity requireVectorizedKb(Long kbId) {
        if (kbId == null) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }
        KnowledgeBaseEntity kb = aiRepository.findKnowledgeBaseById(kbId);
        if (kb == null || (kb.getDeleted() != null && kb.getDeleted() == 1)) {
            throw new BizException(AiErrorCode.KB_NOT_FOUND);
        }
        if (kb.getVectorized() == null || kb.getVectorized() != 1) {
            throw new BizException(AiErrorCode.KB_NOT_VECTORIZED);
        }
        return kb;
    }

    /**
     * 校验并返回文档实体
     *
     * @param documentId 文档ID
     * @return 文档实体
     */
    private DocumentEntity requireDocument(Long documentId) {
        if (documentId == null) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }
        DocumentEntity doc = aiRepository.findDocumentById(documentId);
        if (doc == null || (doc.getDeleted() != null && doc.getDeleted() == 1)) {
            throw new BizException(AiErrorCode.KB_DOCUMENT_NOT_FOUND);
        }
        return doc;
    }

    /**
     * 估算文本的 token 数量
     *
     * @param text 纯文本内容
     * @return 估算的 token 数
     */
    private int estimateTokenCount(String text) {
        if (text == null || text.isBlank()) {
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
