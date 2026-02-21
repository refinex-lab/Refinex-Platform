package cn.refinex.ai.interfaces.controller;

import cn.refinex.ai.application.command.CreateToolCommand;
import cn.refinex.ai.application.command.QueryToolListCommand;
import cn.refinex.ai.application.command.UpdateToolCommand;
import cn.refinex.ai.application.dto.ToolDTO;
import cn.refinex.ai.application.service.AiApplicationService;
import cn.refinex.ai.interfaces.assembler.AiApiAssembler;
import cn.refinex.ai.interfaces.dto.ToolCreateRequest;
import cn.refinex.ai.interfaces.dto.ToolListQuery;
import cn.refinex.ai.interfaces.dto.ToolUpdateRequest;
import cn.refinex.ai.interfaces.vo.ToolVO;
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
 * AI工具管理
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/tools")
@RequiredArgsConstructor
public class ToolController {

    private final AiApplicationService aiApplicationService;
    private final AiApiAssembler aiApiAssembler;

    /**
     * 查询工具分页列表
     *
     * @param query 查询参数
     * @return 工具分页列表
     */
    @GetMapping
    public Mono<PageResult<ToolVO>> listTools(@Valid ToolListQuery query) {
        return Mono.fromCallable(() -> {
            QueryToolListCommand command = aiApiAssembler.toQueryToolListCommand(query);
            PageResponse<ToolDTO> tools = aiApplicationService.listTools(command);
            return PageResult.success(
                    aiApiAssembler.toToolVoList(tools.getData()),
                    tools.getTotal(),
                    tools.getCurrentPage(),
                    tools.getPageSize()
            );
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询全部工具（不分页，用于下拉选择）
     *
     * @param toolType 工具类型(FUNCTION/MCP/HTTP)
     * @param status   状态（0-禁用，1-启用）
     * @return 全部工具列表
     */
    @GetMapping("/all")
    public Mono<Result<List<ToolVO>>> listAllTools(
            @RequestParam(required = false) String toolType,
            @RequestParam(required = false) Integer status) {
        return Mono.fromCallable(() -> {
            List<ToolDTO> tools = aiApplicationService.listAllTools(toolType, status);
            return Result.success(aiApiAssembler.toToolVoList(tools));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询工具详情
     *
     * @param toolId 工具ID
     * @return 工具详情
     */
    @GetMapping("/{toolId}")
    public Mono<Result<ToolVO>> getTool(@PathVariable @Positive(message = "工具ID必须大于0") Long toolId) {
        return Mono.fromCallable(() -> {
            ToolDTO tool = aiApplicationService.getTool(toolId);
            return Result.success(aiApiAssembler.toToolVo(tool));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 创建工具
     *
     * @param request 创建工具请求参数
     * @return 创建的工具详情
     */
    @PostMapping
    public Mono<Result<ToolVO>> createTool(@Valid @RequestBody ToolCreateRequest request) {
        return Mono.fromCallable(() -> {
            CreateToolCommand command = aiApiAssembler.toCreateToolCommand(request);
            ToolDTO created = aiApplicationService.createTool(command);
            return Result.success(aiApiAssembler.toToolVo(created));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 更新工具
     *
     * @param toolId  工具ID
     * @param request 更新工具请求参数
     * @return 更新后的工具详情
     */
    @PutMapping("/{toolId}")
    public Mono<Result<ToolVO>> updateTool(
            @PathVariable @Positive(message = "工具ID必须大于0") Long toolId,
            @Valid @RequestBody ToolUpdateRequest request) {
        return Mono.fromCallable(() -> {
            UpdateToolCommand command = aiApiAssembler.toUpdateToolCommand(request);
            command.setToolId(toolId);
            ToolDTO updated = aiApplicationService.updateTool(command);
            return Result.success(aiApiAssembler.toToolVo(updated));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 删除工具
     *
     * @param toolId 工具ID
     */
    @DeleteMapping("/{toolId}")
    public Mono<Result<Void>> deleteTool(@PathVariable @Positive(message = "工具ID必须大于0") Long toolId) {
        return Mono.fromCallable(() -> {
            aiApplicationService.deleteTool(toolId);
            return Result.<Void>success();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
