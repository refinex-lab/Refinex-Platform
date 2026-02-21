package cn.refinex.ai.interfaces.controller;

import cn.refinex.ai.application.command.CreateModelCommand;
import cn.refinex.ai.application.command.QueryModelListCommand;
import cn.refinex.ai.application.command.UpdateModelCommand;
import cn.refinex.ai.application.dto.ModelDTO;
import cn.refinex.ai.application.service.AiApplicationService;
import cn.refinex.ai.interfaces.assembler.AiApiAssembler;
import cn.refinex.ai.interfaces.dto.ModelCreateRequest;
import cn.refinex.ai.interfaces.dto.ModelListQuery;
import cn.refinex.ai.interfaces.dto.ModelUpdateRequest;
import cn.refinex.ai.interfaces.vo.ModelVO;
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
 * AI 模型管理
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/models")
@RequiredArgsConstructor
public class ModelController {

    private final AiApplicationService aiApplicationService;
    private final AiApiAssembler aiApiAssembler;

    /**
     * 查询模型分页列表
     */
    @GetMapping
    public Mono<PageResult<ModelVO>> listModels(@Valid ModelListQuery query) {
        return Mono.fromCallable(() -> {
            QueryModelListCommand command = aiApiAssembler.toQueryModelListCommand(query);
            PageResponse<ModelDTO> models = aiApplicationService.listModels(command);
            return PageResult.success(
                    aiApiAssembler.toModelVoList(models.getData()),
                    models.getTotal(),
                    models.getCurrentPage(),
                    models.getPageSize()
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询指定供应商下的全部模型（不分页）
     */
    @GetMapping("/by-provider/{providerId}")
    public Mono<Result<List<ModelVO>>> listModelsByProviderId(
            @PathVariable @Positive(message = "供应商ID必须大于0") Long providerId) {
        return Mono.fromCallable(() -> {
            List<ModelDTO> models = aiApplicationService.listModelsByProviderId(providerId);
            return Result.success(aiApiAssembler.toModelVoList(models));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询模型详情
     */
    @GetMapping("/{modelId}")
    public Mono<Result<ModelVO>> getModel(
            @PathVariable @Positive(message = "模型ID必须大于0") Long modelId) {
        return Mono.fromCallable(() -> {
            ModelDTO model = aiApplicationService.getModel(modelId);
            return Result.success(aiApiAssembler.toModelVo(model));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 创建模型
     */
    @PostMapping
    public Mono<Result<ModelVO>> createModel(@Valid @RequestBody ModelCreateRequest request) {
        return Mono.fromCallable(() -> {
            CreateModelCommand command = aiApiAssembler.toCreateModelCommand(request);
            ModelDTO created = aiApplicationService.createModel(command);
            return Result.success(aiApiAssembler.toModelVo(created));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 更新模型
     */
    @PutMapping("/{modelId}")
    public Mono<Result<ModelVO>> updateModel(
            @PathVariable @Positive(message = "模型ID必须大于0") Long modelId,
            @Valid @RequestBody ModelUpdateRequest request) {
        return Mono.fromCallable(() -> {
            UpdateModelCommand command = aiApiAssembler.toUpdateModelCommand(request);
            command.setModelId(modelId);
            ModelDTO updated = aiApplicationService.updateModel(command);
            return Result.success(aiApiAssembler.toModelVo(updated));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 删除模型
     */
    @DeleteMapping("/{modelId}")
    public Mono<Result<Void>> deleteModel(
            @PathVariable @Positive(message = "模型ID必须大于0") Long modelId) {
        return Mono.fromCallable(() -> {
            aiApplicationService.deleteModel(modelId);
            return Result.<Void>success();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
