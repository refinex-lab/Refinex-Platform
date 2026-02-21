package cn.refinex.ai.interfaces.controller;

import cn.refinex.ai.application.command.CreateProviderCommand;
import cn.refinex.ai.application.command.QueryProviderListCommand;
import cn.refinex.ai.application.command.UpdateProviderCommand;
import cn.refinex.ai.application.dto.ProviderDTO;
import cn.refinex.ai.application.service.AiApplicationService;
import cn.refinex.ai.interfaces.assembler.AiApiAssembler;
import cn.refinex.ai.interfaces.dto.ProviderCreateRequest;
import cn.refinex.ai.interfaces.dto.ProviderListQuery;
import cn.refinex.ai.interfaces.dto.ProviderUpdateRequest;
import cn.refinex.ai.interfaces.vo.ProviderVO;
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
 * AI 供应商管理
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final AiApplicationService aiApplicationService;
    private final AiApiAssembler aiApiAssembler;

    /**
     * 查询供应商分页列表
     *
     * @param query 查询参数
     * @return 供应商分页列表
     */
    @GetMapping
    public Mono<PageResult<ProviderVO>> listProviders(@Valid ProviderListQuery query) {
        return Mono.fromCallable(() -> {
            QueryProviderListCommand command = aiApiAssembler.toQueryProviderListCommand(query);
            PageResponse<ProviderDTO> providers = aiApplicationService.listProviders(command);
            return PageResult.success(
                    aiApiAssembler.toProviderVoList(providers.getData()),
                    providers.getTotal(),
                    providers.getCurrentPage(),
                    providers.getPageSize()
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询全部供应商（不分页，用于下拉选择）
     *
     * @param status 状态（0-禁用，1-启用）
     * @return 全部供应商列表
     */
    @GetMapping("/all")
    public Mono<Result<List<ProviderVO>>> listAllProviders(@RequestParam(required = false) Integer status) {
        return Mono.fromCallable(() -> {
            List<ProviderDTO> providers = aiApplicationService.listAllProviders(status);
            return Result.success(aiApiAssembler.toProviderVoList(providers));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询供应商详情
     *
     * @param providerId 供应商ID
     * @return 供应商详情
     */
    @GetMapping("/{providerId}")
    public Mono<Result<ProviderVO>> getProvider(@PathVariable @Positive(message = "供应商ID必须大于0") Long providerId) {
        return Mono.fromCallable(() -> {
            ProviderDTO provider = aiApplicationService.getProvider(providerId);
            return Result.success(aiApiAssembler.toProviderVo(provider));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 创建供应商
     *
     * @param request 创建供应商请求参数
     * @return 创建的供应商详情
     */
    @PostMapping
    public Mono<Result<ProviderVO>> createProvider(@Valid @RequestBody ProviderCreateRequest request) {
        return Mono.fromCallable(() -> {
            CreateProviderCommand command = aiApiAssembler.toCreateProviderCommand(request);
            ProviderDTO created = aiApplicationService.createProvider(command);
            return Result.success(aiApiAssembler.toProviderVo(created));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 更新供应商
     *
     * @param providerId 供应商ID
     * @param request    更新供应商请求参数
     * @return 更新后的供应商详情
     */
    @PutMapping("/{providerId}")
    public Mono<Result<ProviderVO>> updateProvider(
            @PathVariable @Positive(message = "供应商ID必须大于0") Long providerId,
            @Valid @RequestBody ProviderUpdateRequest request) {
        return Mono.fromCallable(() -> {
            UpdateProviderCommand command = aiApiAssembler.toUpdateProviderCommand(request);
            command.setProviderId(providerId);
            ProviderDTO updated = aiApplicationService.updateProvider(command);
            return Result.success(aiApiAssembler.toProviderVo(updated));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 删除供应商
     *
     * @param providerId 供应商ID
     */
    @DeleteMapping("/{providerId}")
    public Mono<Result<Void>> deleteProvider(@PathVariable @Positive(message = "供应商ID必须大于0") Long providerId) {
        return Mono.fromCallable(() -> {
            aiApplicationService.deleteProvider(providerId);
            return Result.<Void>success();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
