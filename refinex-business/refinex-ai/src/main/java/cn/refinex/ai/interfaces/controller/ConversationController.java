package cn.refinex.ai.interfaces.controller;

import cn.refinex.ai.application.command.QueryConversationListCommand;
import cn.refinex.ai.application.command.StreamChatCommand;
import cn.refinex.ai.application.dto.ConversationDTO;
import cn.refinex.ai.application.service.ConversationApplicationService;
import cn.refinex.ai.infrastructure.config.ReactiveLoginUserHolder;
import cn.refinex.ai.interfaces.assembler.AiApiAssembler;
import cn.refinex.ai.interfaces.dto.ChatRequest;
import cn.refinex.ai.interfaces.dto.ConversationListQuery;
import cn.refinex.ai.interfaces.dto.ConversationTitleUpdateRequest;
import cn.refinex.ai.interfaces.vo.ConversationDetailVO;
import cn.refinex.ai.interfaces.vo.ConversationVO;
import cn.refinex.base.response.PageResponse;
import cn.refinex.web.vo.PageResult;
import cn.refinex.web.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 对话管理
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationApplicationService conversationApplicationService;
    private final AiApiAssembler aiApiAssembler;

    /**
     * 流式对话（SSE）
     *
     * @param request  对话请求
     * @param exchange 当前请求上下文
     * @return SSE 流
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@Valid @RequestBody ChatRequest request,
                                              ServerWebExchange exchange) {
        ReactiveLoginUserHolder.initFromExchange(exchange);
        try {
            StreamChatCommand command = aiApiAssembler.toStreamChatCommand(request);
            command.setEstabId(ReactiveLoginUserHolder.getEstabId());
            command.setUserId(ReactiveLoginUserHolder.getUserId());
            return conversationApplicationService.streamChat(command);
        } finally {
            ReactiveLoginUserHolder.clear();
        }
    }

    /**
     * 查询对话分页列表
     *
     * @param query    查询参数
     * @param exchange 当前请求上下文
     * @return 对话分页列表
     */
    @GetMapping
    public Mono<PageResult<ConversationVO>> listConversations(@Valid ConversationListQuery query,
                                                              ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            ReactiveLoginUserHolder.initFromExchange(exchange);
            try {
                QueryConversationListCommand command = aiApiAssembler.toQueryConversationListCommand(query);
                command.setEstabId(ReactiveLoginUserHolder.getEstabId());
                command.setUserId(ReactiveLoginUserHolder.getUserId());
                PageResponse<ConversationDTO> conversations = conversationApplicationService.listConversations(command);
                return PageResult.success(
                        aiApiAssembler.toConversationVoList(conversations.getData()),
                        conversations.getTotal(),
                        conversations.getCurrentPage(),
                        conversations.getPageSize()
                );
            } finally {
                ReactiveLoginUserHolder.clear();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 查询对话详情（含消息历史）
     *
     * @param conversationId 会话唯一标识
     * @param exchange       当前请求上下文
     * @return 对话详情
     */
    @GetMapping("/{conversationId}")
    public Mono<Result<ConversationDetailVO>> getConversation(@PathVariable String conversationId,
                                                              ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            ReactiveLoginUserHolder.initFromExchange(exchange);
            try {
                Long userId = ReactiveLoginUserHolder.getUserId();
                ConversationDetailVO detail = conversationApplicationService.getConversationDetail(conversationId, userId);
                return Result.success(detail);
            } finally {
                ReactiveLoginUserHolder.clear();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 删除对话（逻辑删除 + 清除 ChatMemory）
     *
     * @param conversationId 会话唯一标识
     * @param exchange       当前请求上下文
     * @return 操作结果
     */
    @DeleteMapping("/{conversationId}")
    public Mono<Result<Void>> deleteConversation(@PathVariable String conversationId,
                                                  ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            ReactiveLoginUserHolder.initFromExchange(exchange);
            try {
                Long userId = ReactiveLoginUserHolder.getUserId();
                conversationApplicationService.deleteConversation(conversationId, userId);
                return Result.<Void>success();
            } finally {
                ReactiveLoginUserHolder.clear();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 更新对话标题
     *
     * @param conversationId 会话唯一标识
     * @param request        标题更新请求
     * @param exchange       当前请求上下文
     * @return 操作结果
     */
    @PutMapping("/{conversationId}/title")
    public Mono<Result<Void>> updateTitle(
            @PathVariable String conversationId,
            @Valid @RequestBody ConversationTitleUpdateRequest request,
            ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            ReactiveLoginUserHolder.initFromExchange(exchange);
            try {
                Long userId = ReactiveLoginUserHolder.getUserId();
                conversationApplicationService.updateTitle(conversationId, userId, request.getTitle());
                return Result.<Void>success();
            } finally {
                ReactiveLoginUserHolder.clear();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 切换对话置顶状态
     *
     * @param conversationId 会话唯一标识
     * @param exchange       当前请求上下文
     * @return 操作结果
     */
    @PutMapping("/{conversationId}/pin")
    public Mono<Result<Void>> togglePin(@PathVariable String conversationId,
                                        ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            ReactiveLoginUserHolder.initFromExchange(exchange);
            try {
                Long userId = ReactiveLoginUserHolder.getUserId();
                conversationApplicationService.togglePin(conversationId, userId);
                return Result.<Void>success();
            } finally {
                ReactiveLoginUserHolder.clear();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
