package cn.refinex.ai.interfaces.controller;

import cn.refinex.ai.application.command.CreateModelProvisionCommand;
import cn.refinex.ai.application.command.QueryModelProvisionListCommand;
import cn.refinex.ai.application.command.UpdateModelProvisionCommand;
import cn.refinex.ai.application.dto.ModelProvisionDTO;
import cn.refinex.ai.application.service.AiApplicationService;
import cn.refinex.ai.interfaces.assembler.AiApiAssembler;
import cn.refinex.ai.interfaces.dto.ModelProvisionCreateRequest;
import cn.refinex.ai.interfaces.dto.ModelProvisionListQuery;
import cn.refinex.ai.interfaces.dto.ModelProvisionUpdateRequest;
import cn.refinex.ai.interfaces.vo.ModelProvisionVO;
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

/**
 * 租户模型开通管理
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/model-provisions")
@RequiredArgsConstructor
public class ModelProvisionController {

    private final AiApplicationService aiApplicationService;
    private final AiApiAssembler aiApiAssembler;

    /**
     * 查询租户模型开通分页列表
     *
     * @param query 查询参数
     * @return 租户模型开通分页列表
     */
    @GetMapping
    public Mono<PageResult<ModelProvisionVO>> listModelProvisions(@Valid ModelProvisionListQuery query) {
        return Mono.fromCallable(() -> {
            QueryModelProvisionListCommand command = aiApiAssembler.toQueryModelProvisionListCommand(query);
            PageResponse<ModelProvisionDTO> provisions = aiApplicationService.listModelProvisions(command);
            return PageResult.success(
                    aiApiAssembler.toModelProvisionVoList(provisions.getData()),
                    provisions.getTotal(),
                    provisions.getCurrentPage(),
                    provisions.getPageSize()
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询租户模型开通详情
     *
     * @param provisionId 开通ID
     * @return 租户模型开通详情
     */
    @GetMapping("/{provisionId}")
    public Mono<Result<ModelProvisionVO>> getModelProvision(
            @PathVariable @Positive(message = "开通ID必须大于0") Long provisionId) {
        return Mono.fromCallable(() -> {
            ModelProvisionDTO provision = aiApplicationService.getModelProvision(provisionId);
            return Result.success(aiApiAssembler.toModelProvisionVo(provision));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 创建租户模型开通
     *
     * @param request 创建租户模型开通请求参数
     * @return 创建的租户模型开通详情
     */
    @PostMapping
    public Mono<Result<ModelProvisionVO>> createModelProvision(@Valid @RequestBody ModelProvisionCreateRequest request) {
        return Mono.fromCallable(() -> {
            CreateModelProvisionCommand command = aiApiAssembler.toCreateModelProvisionCommand(request);
            ModelProvisionDTO created = aiApplicationService.createModelProvision(command);
            return Result.success(aiApiAssembler.toModelProvisionVo(created));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 更新租户模型开通
     *
     * @param provisionId 开通ID
     * @param request     更新租户模型开通请求参数
     * @return 更新后的租户模型开通详情
     */
    @PutMapping("/{provisionId}")
    public Mono<Result<ModelProvisionVO>> updateModelProvision(
            @PathVariable @Positive(message = "开通ID必须大于0") Long provisionId,
            @Valid @RequestBody ModelProvisionUpdateRequest request) {
        return Mono.fromCallable(() -> {
            UpdateModelProvisionCommand command = aiApiAssembler.toUpdateModelProvisionCommand(request);
            command.setProvisionId(provisionId);
            ModelProvisionDTO updated = aiApplicationService.updateModelProvision(command);
            return Result.success(aiApiAssembler.toModelProvisionVo(updated));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 删除租户模型开通
     *
     * @param provisionId 开通ID
     */
    @DeleteMapping("/{provisionId}")
    public Mono<Result<Void>> deleteModelProvision(
            @PathVariable @Positive(message = "开通ID必须大于0") Long provisionId) {
        return Mono.fromCallable(() -> {
            aiApplicationService.deleteModelProvision(provisionId);
            return Result.<Void>success();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
