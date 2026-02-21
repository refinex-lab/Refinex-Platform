package cn.refinex.ai.interfaces.controller;

import cn.refinex.ai.application.command.CreatePromptTemplateCommand;
import cn.refinex.ai.application.command.QueryPromptTemplateListCommand;
import cn.refinex.ai.application.command.UpdatePromptTemplateCommand;
import cn.refinex.ai.application.dto.PromptTemplateDTO;
import cn.refinex.ai.application.service.AiApplicationService;
import cn.refinex.ai.interfaces.assembler.AiApiAssembler;
import cn.refinex.ai.interfaces.dto.PromptTemplateCreateRequest;
import cn.refinex.ai.interfaces.dto.PromptTemplateListQuery;
import cn.refinex.ai.interfaces.dto.PromptTemplateUpdateRequest;
import cn.refinex.ai.interfaces.vo.PromptTemplateVO;
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
 * Prompt模板管理
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/prompt-templates")
@RequiredArgsConstructor
public class PromptTemplateController {

    private final AiApplicationService aiApplicationService;
    private final AiApiAssembler aiApiAssembler;

    /**
     * 查询Prompt模板分页列表
     *
     * @param query 查询参数
     * @return Prompt模板分页列表
     */
    @GetMapping
    public Mono<PageResult<PromptTemplateVO>> listPromptTemplates(@Valid PromptTemplateListQuery query) {
        return Mono.fromCallable(() -> {
            QueryPromptTemplateListCommand command = aiApiAssembler.toQueryPromptTemplateListCommand(query);
            PageResponse<PromptTemplateDTO> templates = aiApplicationService.listPromptTemplates(command);
            return PageResult.success(
                    aiApiAssembler.toPromptTemplateVoList(templates.getData()),
                    templates.getTotal(),
                    templates.getCurrentPage(),
                    templates.getPageSize()
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询全部Prompt模板（不分页，用于下拉选择）
     *
     * @param status   状态（0-禁用，1-启用）
     * @param category 分类
     * @return 全部Prompt模板列表
     */
    @GetMapping("/all")
    public Mono<Result<List<PromptTemplateVO>>> listAllPromptTemplates(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String category) {
        return Mono.fromCallable(() -> {
            List<PromptTemplateDTO> templates = aiApplicationService.listAllPromptTemplates(status, category);
            return Result.success(aiApiAssembler.toPromptTemplateVoList(templates));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询Prompt模板详情
     *
     * @param promptTemplateId Prompt模板ID
     * @return Prompt模板详情
     */
    @GetMapping("/{promptTemplateId}")
    public Mono<Result<PromptTemplateVO>> getPromptTemplate(
            @PathVariable @Positive(message = "Prompt模板ID必须大于0") Long promptTemplateId) {
        return Mono.fromCallable(() -> {
            PromptTemplateDTO template = aiApplicationService.getPromptTemplate(promptTemplateId);
            return Result.success(aiApiAssembler.toPromptTemplateVo(template));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 创建Prompt模板
     *
     * @param request 创建Prompt模板请求参数
     * @return 创建的Prompt模板详情
     */
    @PostMapping
    public Mono<Result<PromptTemplateVO>> createPromptTemplate(@Valid @RequestBody PromptTemplateCreateRequest request) {
        return Mono.fromCallable(() -> {
            CreatePromptTemplateCommand command = aiApiAssembler.toCreatePromptTemplateCommand(request);
            PromptTemplateDTO created = aiApplicationService.createPromptTemplate(command);
            return Result.success(aiApiAssembler.toPromptTemplateVo(created));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 更新Prompt模板
     *
     * @param promptTemplateId Prompt模板ID
     * @param request          更新Prompt模板请求参数
     * @return 更新后的Prompt模板详情
     */
    @PutMapping("/{promptTemplateId}")
    public Mono<Result<PromptTemplateVO>> updatePromptTemplate(
            @PathVariable @Positive(message = "Prompt模板ID必须大于0") Long promptTemplateId,
            @Valid @RequestBody PromptTemplateUpdateRequest request) {
        return Mono.fromCallable(() -> {
            UpdatePromptTemplateCommand command = aiApiAssembler.toUpdatePromptTemplateCommand(request);
            command.setPromptTemplateId(promptTemplateId);
            PromptTemplateDTO updated = aiApplicationService.updatePromptTemplate(command);
            return Result.success(aiApiAssembler.toPromptTemplateVo(updated));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 删除Prompt模板
     *
     * @param promptTemplateId Prompt模板ID
     */
    @DeleteMapping("/{promptTemplateId}")
    public Mono<Result<Void>> deletePromptTemplate(
            @PathVariable @Positive(message = "Prompt模板ID必须大于0") Long promptTemplateId) {
        return Mono.fromCallable(() -> {
            aiApplicationService.deletePromptTemplate(promptTemplateId);
            return Result.<Void>success();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
