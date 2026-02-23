package cn.refinex.ai.interfaces.controller;

import cn.refinex.ai.application.command.CreateMcpServerCommand;
import cn.refinex.ai.application.command.QueryMcpServerListCommand;
import cn.refinex.ai.application.command.UpdateMcpServerCommand;
import cn.refinex.ai.application.dto.McpServerDTO;
import cn.refinex.ai.application.service.AiApplicationService;
import cn.refinex.ai.infrastructure.config.ReactiveLoginUserHolder;
import cn.refinex.ai.interfaces.assembler.AiApiAssembler;
import cn.refinex.ai.interfaces.dto.McpServerCreateRequest;
import cn.refinex.ai.interfaces.dto.McpServerListQuery;
import cn.refinex.ai.interfaces.dto.McpServerUpdateRequest;
import cn.refinex.ai.interfaces.vo.McpServerVO;
import cn.refinex.base.response.PageResponse;
import cn.refinex.web.vo.PageResult;
import cn.refinex.web.vo.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * MCP服务器管理
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/mcp-servers")
@RequiredArgsConstructor
public class McpServerController {

    private final AiApplicationService aiApplicationService;
    private final AiApiAssembler aiApiAssembler;

    /**
     * 查询MCP服务器分页列表
     *
     * @param query    查询参数
     * @param exchange 当前请求上下文
     * @return MCP服务器分页列表
     */
    @GetMapping
    public Mono<PageResult<McpServerVO>> listMcpServers(@Valid McpServerListQuery query,
                                                        ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            ReactiveLoginUserHolder.initFromExchange(exchange);
            try {
                QueryMcpServerListCommand command = aiApiAssembler.toQueryMcpServerListCommand(query);
                PageResponse<McpServerDTO> servers = aiApplicationService.listMcpServers(command);
                return PageResult.success(
                        aiApiAssembler.toMcpServerVoList(servers.getData()),
                        servers.getTotal(),
                        servers.getCurrentPage(),
                        servers.getPageSize()
                );
            } finally {
                ReactiveLoginUserHolder.clear();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询全部MCP服务器（不分页，用于下拉选择）
     *
     * @param transportType 传输类型(stdio/sse)
     * @param status        状态（0-禁用，1-启用）
     * @param exchange      当前请求上下文
     * @return 全部MCP服务器列表
     */
    @GetMapping("/all")
    public Mono<Result<List<McpServerVO>>> listAllMcpServers(
            @RequestParam(required = false) String transportType,
            @RequestParam(required = false) Integer status,
            ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            ReactiveLoginUserHolder.initFromExchange(exchange);
            try {
                List<McpServerDTO> servers = aiApplicationService.listAllMcpServers(transportType, status);
                return Result.success(aiApiAssembler.toMcpServerVoList(servers));
            } finally {
                ReactiveLoginUserHolder.clear();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询MCP服务器详情
     *
     * @param mcpServerId MCP服务器ID
     * @return MCP服务器详情
     */
    @GetMapping("/{mcpServerId}")
    public Mono<Result<McpServerVO>> getMcpServer(
            @PathVariable @Positive(message = "MCP服务器ID必须大于0") Long mcpServerId) {
        return Mono.fromCallable(() -> {
            McpServerDTO server = aiApplicationService.getMcpServer(mcpServerId);
            return Result.success(aiApiAssembler.toMcpServerVo(server));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 创建MCP服务器
     *
     * @param request  创建MCP服务器请求参数
     * @param exchange 当前请求上下文
     * @return 创建的MCP服务器详情
     */
    @PostMapping
    public Mono<Result<McpServerVO>> createMcpServer(@Valid @RequestBody McpServerCreateRequest request,
                                                      ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            ReactiveLoginUserHolder.initFromExchange(exchange);
            try {
                CreateMcpServerCommand command = aiApiAssembler.toCreateMcpServerCommand(request);
                McpServerDTO created = aiApplicationService.createMcpServer(command);
                return Result.success(aiApiAssembler.toMcpServerVo(created));
            } finally {
                ReactiveLoginUserHolder.clear();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 更新MCP服务器
     *
     * @param mcpServerId MCP服务器ID
     * @param request     更新MCP服务器请求参数
     * @return 更新后的MCP服务器详情
     */
    @PutMapping("/{mcpServerId}")
    public Mono<Result<McpServerVO>> updateMcpServer(
            @PathVariable @Positive(message = "MCP服务器ID必须大于0") Long mcpServerId,
            @Valid @RequestBody McpServerUpdateRequest request) {
        return Mono.fromCallable(() -> {
            UpdateMcpServerCommand command = aiApiAssembler.toUpdateMcpServerCommand(request);
            command.setMcpServerId(mcpServerId);
            McpServerDTO updated = aiApplicationService.updateMcpServer(command);
            return Result.success(aiApiAssembler.toMcpServerVo(updated));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 删除MCP服务器
     *
     * @param mcpServerId MCP服务器ID
     */
    @DeleteMapping("/{mcpServerId}")
    public Mono<Result<Void>> deleteMcpServer(
            @PathVariable @Positive(message = "MCP服务器ID必须大于0") Long mcpServerId) {
        return Mono.fromCallable(() -> {
            aiApplicationService.deleteMcpServer(mcpServerId);
            return Result.<Void>success();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
