package cn.refinex.ai.interfaces.controller;

import cn.refinex.ai.application.command.*;
import cn.refinex.ai.application.dto.DocumentDTO;
import cn.refinex.ai.application.dto.FolderDTO;
import cn.refinex.ai.application.dto.KnowledgeBaseDTO;
import cn.refinex.ai.application.service.KbApplicationService;
import cn.refinex.ai.interfaces.assembler.KbApiAssembler;
import cn.refinex.ai.interfaces.dto.*;
import cn.refinex.ai.interfaces.vo.DocumentVO;
import cn.refinex.ai.interfaces.vo.FolderVO;
import cn.refinex.ai.interfaces.vo.KnowledgeBaseVO;
import cn.refinex.base.response.PageResponse;
import cn.refinex.web.vo.PageResult;
import cn.refinex.web.vo.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * 知识库管理
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/knowledge-bases")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KbApplicationService kbApplicationService;
    private final KbApiAssembler kbApiAssembler;

    // ── KnowledgeBase ──

    /**
     * 查询知识库分页列表
     *
     * @param query 查询参数
     * @return 知识库分页列表
     */
    @GetMapping
    public Mono<PageResult<KnowledgeBaseVO>> listKnowledgeBases(@Valid KnowledgeBaseListQuery query) {
        return Mono.fromCallable(() -> {
            QueryKnowledgeBaseListCommand command = kbApiAssembler.toQueryKnowledgeBaseListCommand(query);
            PageResponse<KnowledgeBaseDTO> kbs = kbApplicationService.listKnowledgeBases(command);
            return PageResult.success(
                    kbApiAssembler.toKnowledgeBaseVoList(kbs.getData()),
                    kbs.getTotal(),
                    kbs.getCurrentPage(),
                    kbs.getPageSize()
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询全部知识库（不分页，用于下拉选择）
     *
     * @param status 状态（0-禁用，1-启用）
     * @return 全部知识库列表
     */
    @GetMapping("/all")
    public Mono<Result<List<KnowledgeBaseVO>>> listAllKnowledgeBases(@RequestParam(required = false) Integer status) {
        return Mono.fromCallable(() -> {
            List<KnowledgeBaseDTO> kbs = kbApplicationService.listAllKnowledgeBases(status);
            return Result.success(kbApiAssembler.toKnowledgeBaseVoList(kbs));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询知识库详情
     *
     * @param kbId 知识库ID
     * @return 知识库详情
     */
    @GetMapping("/{kbId}")
    public Mono<Result<KnowledgeBaseVO>> getKnowledgeBase(@PathVariable @Positive(message = "知识库ID必须大于0") Long kbId) {
        return Mono.fromCallable(() -> {
            KnowledgeBaseDTO kb = kbApplicationService.getKnowledgeBase(kbId);
            return Result.success(kbApiAssembler.toKnowledgeBaseVo(kb));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 创建知识库
     *
     * @param request 创建知识库请求参数
     * @return 创建的知识库详情
     */
    @PostMapping
    public Mono<Result<KnowledgeBaseVO>> createKnowledgeBase(@Valid @RequestBody KnowledgeBaseCreateRequest request) {
        return Mono.fromCallable(() -> {
            CreateKnowledgeBaseCommand command = kbApiAssembler.toCreateKnowledgeBaseCommand(request);
            KnowledgeBaseDTO created = kbApplicationService.createKnowledgeBase(command);
            return Result.success(kbApiAssembler.toKnowledgeBaseVo(created));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 更新知识库
     *
     * @param kbId    知识库ID
     * @param request 更新知识库请求参数
     * @return 更新后的知识库详情
     */
    @PutMapping("/{kbId}")
    public Mono<Result<KnowledgeBaseVO>> updateKnowledgeBase(
            @PathVariable @Positive(message = "知识库ID必须大于0") Long kbId,
            @Valid @RequestBody KnowledgeBaseUpdateRequest request) {
        return Mono.fromCallable(() -> {
            UpdateKnowledgeBaseCommand command = kbApiAssembler.toUpdateKnowledgeBaseCommand(request);
            command.setKbId(kbId);
            KnowledgeBaseDTO updated = kbApplicationService.updateKnowledgeBase(command);
            return Result.success(kbApiAssembler.toKnowledgeBaseVo(updated));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 删除知识库
     *
     * @param kbId 知识库ID
     * @return 空结果
     */
    @DeleteMapping("/{kbId}")
    public Mono<Result<Void>> deleteKnowledgeBase(@PathVariable @Positive(message = "知识库ID必须大于0") Long kbId) {
        return Mono.fromCallable(() -> {
            kbApplicationService.deleteKnowledgeBase(kbId);
            return Result.<Void>success();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // ── Folder ──

    /**
     * 查询目录列表（全量，前端构建树）
     *
     * @param kbId 知识库ID
     * @return 目录列表
     */
    @GetMapping("/{kbId}/folders")
    public Mono<Result<List<FolderVO>>> listFolders(@PathVariable @Positive(message = "知识库ID必须大于0") Long kbId) {
        return Mono.fromCallable(() -> {
            List<FolderDTO> folders = kbApplicationService.listFolders(kbId);
            return Result.success(kbApiAssembler.toFolderVoList(folders));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 创建目录
     *
     * @param kbId    知识库ID
     * @param request 创建目录请求参数
     * @return 创建的目录详情
     */
    @PostMapping("/{kbId}/folders")
    public Mono<Result<FolderVO>> createFolder(
            @PathVariable @Positive(message = "知识库ID必须大于0") Long kbId,
            @Valid @RequestBody FolderCreateRequest request) {
        return Mono.fromCallable(() -> {
            CreateFolderCommand command = kbApiAssembler.toCreateFolderCommand(request);
            command.setKnowledgeBaseId(kbId);
            FolderDTO created = kbApplicationService.createFolder(command);
            return Result.success(kbApiAssembler.toFolderVo(created));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 更新目录
     *
     * @param kbId     知识库ID
     * @param folderId 目录ID
     * @param request  更新目录请求参数
     * @return 更新后的目录详情
     */
    @PutMapping("/{kbId}/folders/{folderId}")
    public Mono<Result<FolderVO>> updateFolder(
            @PathVariable @Positive(message = "知识库ID必须大于0") Long kbId,
            @PathVariable @Positive(message = "目录ID必须大于0") Long folderId,
            @Valid @RequestBody FolderUpdateRequest request) {
        return Mono.fromCallable(() -> {
            UpdateFolderCommand command = kbApiAssembler.toUpdateFolderCommand(request);
            command.setFolderId(folderId);
            FolderDTO updated = kbApplicationService.updateFolder(command);
            return Result.success(kbApiAssembler.toFolderVo(updated));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 删除目录
     *
     * @param kbId     知识库ID
     * @param folderId 目录ID
     * @return 空结果
     */
    @DeleteMapping("/{kbId}/folders/{folderId}")
    public Mono<Result<Void>> deleteFolder(
            @PathVariable @Positive(message = "知识库ID必须大于0") Long kbId,
            @PathVariable @Positive(message = "目录ID必须大于0") Long folderId) {
        return Mono.fromCallable(() -> {
            kbApplicationService.deleteFolder(folderId);
            return Result.<Void>success();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // ── Document ──

    /**
     * 查询文档分页列表
     *
     * @param kbId  知识库ID
     * @param query 查询参数
     * @return 文档分页列表
     */
    @GetMapping("/{kbId}/documents")
    public Mono<PageResult<DocumentVO>> listDocuments(
            @PathVariable @Positive(message = "知识库ID必须大于0") Long kbId,
            @Valid DocumentListQuery query) {
        return Mono.fromCallable(() -> {
            QueryDocumentListCommand command = kbApiAssembler.toQueryDocumentListCommand(query);
            command.setKnowledgeBaseId(kbId);
            PageResponse<DocumentDTO> docs = kbApplicationService.listDocuments(command);
            return PageResult.success(
                    kbApiAssembler.toDocumentVoList(docs.getData()),
                    docs.getTotal(),
                    docs.getCurrentPage(),
                    docs.getPageSize()
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询文档详情
     *
     * @param kbId  知识库ID
     * @param docId 文档ID
     * @return 文档详情
     */
    @GetMapping("/{kbId}/documents/{docId}")
    public Mono<Result<DocumentVO>> getDocument(
            @PathVariable @Positive(message = "知识库ID必须大于0") Long kbId,
            @PathVariable @Positive(message = "文档ID必须大于0") Long docId) {
        return Mono.fromCallable(() -> {
            DocumentDTO doc = kbApplicationService.getDocument(docId);
            return Result.success(kbApiAssembler.toDocumentVo(doc));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 获取文档内容（大文本单独接口）
     *
     * @param kbId  知识库ID
     * @param docId 文档ID
     * @return 文档纯文本内容
     */
    @GetMapping("/{kbId}/documents/{docId}/content")
    public Mono<Result<String>> getDocumentContent(
            @PathVariable @Positive(message = "知识库ID必须大于0") Long kbId,
            @PathVariable @Positive(message = "文档ID必须大于0") Long docId) {
        return Mono.fromCallable(() -> {
            String content = kbApplicationService.getDocumentContent(docId);
            return Result.success(content);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 创建文档
     *
     * @param kbId    知识库ID
     * @param request 创建文档请求参数
     * @return 创建的文档详情
     */
    @PostMapping("/{kbId}/documents")
    public Mono<Result<DocumentVO>> createDocument(
            @PathVariable @Positive(message = "知识库ID必须大于0") Long kbId,
            @Valid @RequestBody DocumentCreateRequest request) {
        return Mono.fromCallable(() -> {
            CreateDocumentCommand command = kbApiAssembler.toCreateDocumentCommand(request);
            command.setKnowledgeBaseId(kbId);
            DocumentDTO created = kbApplicationService.createDocument(command);
            return Result.success(kbApiAssembler.toDocumentVo(created));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 更新文档
     *
     * @param kbId    知识库ID
     * @param docId   文档ID
     * @param request 更新文档请求参数
     * @return 更新后的文档详情
     */
    @PutMapping("/{kbId}/documents/{docId}")
    public Mono<Result<DocumentVO>> updateDocument(
            @PathVariable @Positive(message = "知识库ID必须大于0") Long kbId,
            @PathVariable @Positive(message = "文档ID必须大于0") Long docId,
            @Valid @RequestBody DocumentUpdateRequest request) {
        return Mono.fromCallable(() -> {
            UpdateDocumentCommand command = kbApiAssembler.toUpdateDocumentCommand(request);
            command.setDocumentId(docId);
            DocumentDTO updated = kbApplicationService.updateDocument(command);
            return Result.success(kbApiAssembler.toDocumentVo(updated));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 删除文档
     *
     * @param kbId  知识库ID
     * @param docId 文档ID
     * @return 空结果
     */
    @DeleteMapping("/{kbId}/documents/{docId}")
    public Mono<Result<Void>> deleteDocument(
            @PathVariable @Positive(message = "知识库ID必须大于0") Long kbId,
            @PathVariable @Positive(message = "文档ID必须大于0") Long docId) {
        return Mono.fromCallable(() -> {
            kbApplicationService.deleteDocument(docId);
            return Result.<Void>success();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // ── Sort ──

    /**
     * 拖拽排序（目录+文档混合）
     *
     * @param kbId    知识库ID
     * @param request 排序请求
     * @return 空结果
     */
    @PostMapping("/{kbId}/sort")
    public Mono<Result<Void>> sortItems(
            @PathVariable @Positive(message = "知识库ID必须大于0") Long kbId,
            @Valid @RequestBody SortRequest request) {
        return Mono.fromCallable(() -> {
            List<SortItemCommand> commands = kbApiAssembler.toSortItemCommandList(request.getItems());
            kbApplicationService.sortItems(kbId, commands);
            return Result.<Void>success();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
