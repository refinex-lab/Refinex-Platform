package cn.refinex.ai.application.service;

import cn.refinex.ai.application.assembler.AiDomainAssembler;
import cn.refinex.ai.application.command.QueryConversationListCommand;
import cn.refinex.ai.application.command.StreamChatCommand;
import cn.refinex.ai.application.dto.ConversationDTO;
import cn.refinex.ai.domain.error.AiErrorCode;
import cn.refinex.ai.domain.model.entity.*;
import cn.refinex.ai.domain.model.enums.ContinueIntentDetector;
import cn.refinex.ai.domain.model.enums.RequestType;
import cn.refinex.ai.domain.repository.AiRepository;
import cn.refinex.ai.infrastructure.ai.ChatModelRouter;
import cn.refinex.ai.interfaces.vo.ChatMessageVO;
import cn.refinex.ai.interfaces.vo.ConversationDetailVO;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.PageResponse;
import cn.refinex.base.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 对话应用服务
 *
 * @author refinex
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationApplicationService {

    private final AiRepository aiRepository;
    private final AiDomainAssembler aiDomainAssembler;
    private final ChatModelRouter chatModelRouter;
    private final ChatMemory chatMemory;
    private final JdbcChatMemoryRepository jdbcChatMemoryRepository;

    /**
     * 每百万 token 的除数常量
     */
    private static final BigDecimal PER_MILLION = new BigDecimal("1000000");

    /**
     * 内建的会话标题生成提示词模板
     * <p>
     * 使用 Spring AI PromptTemplate 默认占位符语法 {variableName}。
     * 要求模型根据用户首条消息生成一个简洁的对话标题。
     */
    private static final String TITLE_GENERATION_PROMPT = "请根据以下用户消息，生成一个简洁的对话标题（不超过20个字，不要加引号和标点符号，直接输出标题文本）：\n\n{message}";

    /**
     * 流式对话
     *
     * @param command 流式对话命令
     * @return SSE 流
     */
    public Flux<ServerSentEvent<String>> streamChat(StreamChatCommand command) {
        return Mono.fromCallable(() -> prepareChat(command))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(ctx -> {
                    if (isPrefixContinueEligible(command, ctx)) {
                        return buildPrefixContinuePipeline(command, ctx);
                    }
                    return buildStreamPipeline(command, ctx);
                });
    }

    /**
     * 查询对话分页列表
     *
     * @param command 查询对话列表命令
     * @return 对话分页列表
     */
    public PageResponse<ConversationDTO> listConversations(QueryConversationListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command.getPageSize(), PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);

        PageResponse<ConversationEntity> entities = aiRepository.listConversations(command.getEstabId(), command.getUserId(), currentPage, pageSize);

        List<ConversationDTO> result = new ArrayList<>();
        for (ConversationEntity entity : entities.getData()) {
            result.add(aiDomainAssembler.toConversationDto(entity));
        }

        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询对话详情（含消息历史）
     *
     * @param conversationId 会话唯一标识
     * @param userId         当前用户ID
     * @return 对话详情VO
     */
    public ConversationDetailVO getConversationDetail(String conversationId, Long userId) {
        ConversationEntity conversation = requireConversation(conversationId);
        requireOwnership(conversation, userId);

        ConversationDTO dto = aiDomainAssembler.toConversationDto(conversation);
        ConversationDetailVO vo = new ConversationDetailVO();
        vo.setConversationId(dto.getConversationId());
        vo.setTitle(dto.getTitle());
        vo.setModelId(dto.getModelId());
        vo.setPinned(dto.getPinned());
        vo.setStatus(dto.getStatus());
        vo.setSystemPrompt(dto.getSystemPrompt());
        vo.setExtJson(dto.getExtJson());

        // 从 JdbcChatMemoryRepository 加载消息历史
        List<ChatMessageVO> messages = new ArrayList<>();
        for (var msg : jdbcChatMemoryRepository.findByConversationId(conversationId)) {
            ChatMessageVO msgVo = new ChatMessageVO();
            msgVo.setType(msg.getMessageType().name());
            msgVo.setContent(msg.getText());
            messages.add(msgVo);
        }
        vo.setMessages(messages);

        return vo;
    }

    /**
     * 删除对话（逻辑删除 + 清除 ChatMemory）
     *
     * @param conversationId 会话唯一标识
     * @param userId         当前用户ID
     */
    public void deleteConversation(String conversationId, Long userId) {
        ConversationEntity conversation = requireConversation(conversationId);
        requireOwnership(conversation, userId);

        aiRepository.deleteConversationByConversationId(conversationId);
        chatMemory.clear(conversationId);
    }

    /**
     * 更新对话标题
     *
     * @param conversationId 会话唯一标识
     * @param userId         当前用户ID
     * @param title          新标题
     */
    public void updateTitle(String conversationId, Long userId, String title) {
        ConversationEntity conversation = requireConversation(conversationId);
        requireOwnership(conversation, userId);

        conversation.setTitle(title);
        aiRepository.updateConversation(conversation);
    }

    /**
     * 切换对话置顶状态
     *
     * @param conversationId 会话唯一标识
     * @param userId         当前用户ID
     */
    public void togglePin(String conversationId, Long userId) {
        ConversationEntity conversation = requireConversation(conversationId);
        requireOwnership(conversation, userId);

        conversation.setPinned(conversation.getPinned() != null && conversation.getPinned() == 1 ? 0 : 1);
        aiRepository.updateConversation(conversation);
    }

    // ── 私有方法 ──

    /**
     * 构建流式响应管道（Phase 2 + Phase 3）
     * <p>
     * 从 streamChat 中拆分出来以降低认知复杂度。
     *
     * @param command 流式对话命令
     * @param ctx     对话上下文
     * @return SSE 流
     */
    private Flux<ServerSentEvent<String>> buildStreamPipeline(StreamChatCommand command, ChatContext ctx) {
        AtomicReference<Usage> usageRef = new AtomicReference<>();
        AtomicReference<String> finishReasonRef = new AtomicReference<>();
        AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

        ChatClient chatClient = buildChatClient(ctx.chatModel(), ctx.systemPrompt());

        return chatClient.prompt()
                .user(command.getMessage())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, ctx.conversationId()))
                .stream()
                .chatResponse()
                .doOnNext(chatResponse -> captureMetadata(chatResponse, usageRef, finishReasonRef))
                .flatMapIterable(chatResponse -> extractSseEvents(chatResponse, ctx.capReasoning()))
                .concatWith(Flux.just(ServerSentEvent.<String>builder().event("done").data("[DONE]").build()))
                .doOnComplete(() ->
                        Mono.fromRunnable(() -> onStreamComplete(command, ctx, usageRef.get(), finishReasonRef.get(),
                                        (int) (System.currentTimeMillis() - startTime.get())))
                                .subscribeOn(Schedulers.boundedElastic()).subscribe()
                )
                .doOnError(error -> {
                    log.error("流式对话异常, conversationId={}", ctx.conversationId(), error);
                    Mono.fromRunnable(() -> recordUsageLog(
                            command, ctx, usageRef.get(), "error",
                            (int) (System.currentTimeMillis() - startTime.get()), false, error.getMessage()
                    )).subscribeOn(Schedulers.boundedElastic()).subscribe();
                });
    }

    /**
     * 从流式响应帧中捕获 Usage 和 finishReason
     *
     * @param chatResponse    流式响应帧
     * @param usageRef        Usage 引用
     * @param finishReasonRef finishReason 引用
     */
    private void captureMetadata(ChatResponse chatResponse, AtomicReference<Usage> usageRef, AtomicReference<String> finishReasonRef) {
        Usage usage = chatResponse.getMetadata().getUsage();
        if (usage != null && usage.getTotalTokens() != null && usage.getTotalTokens() > 0) {
            usageRef.set(usage);
        }

        chatResponse.getResult().getMetadata();
        if (chatResponse.getResult().getMetadata().getFinishReason() != null) {
            finishReasonRef.set(chatResponse.getResult().getMetadata().getFinishReason());
        }
    }

    /**
     * 从流式响应帧中提取 SSE 事件（推理感知）
     * <p>
     * 推理模型（capReasoning=true）的 {@link DeepSeekAssistantMessage} 会携带 reasoningContent，
     * 通过独立的 {@code event: reasoning} 事件推送给前端，与回答内容 {@code event: answer} 分离。
     * 非推理模型只发 {@code event: answer}。
     *
     * @param chatResponse 流式响应帧
     * @param capReasoning 是否支持深度推理
     * @return SSE 事件列表
     */
    private List<ServerSentEvent<String>> extractSseEvents(ChatResponse chatResponse, boolean capReasoning) {
        List<ServerSentEvent<String>> events = new ArrayList<>();
        var output = chatResponse.getResult().getOutput();

        // 推理内容提取（仅 DeepSeek 推理模型）
        if (capReasoning && output instanceof DeepSeekAssistantMessage deepSeekMsg) {
            String reasoning = deepSeekMsg.getReasoningContent();
            if (reasoning != null && !reasoning.isEmpty()) {
                events.add(ServerSentEvent.<String>builder().event("reasoning").data(reasoning).build());
            }
        }

        // 回答内容提取
        String text = output.getText();
        if (text != null && !text.isEmpty()) {
            events.add(ServerSentEvent.<String>builder().event("answer").data(text).build());
        }
        return events;
    }

    /**
     * 判断是否满足前缀续写条件
     * <p>
     * 条件：非新建对话 + DeepSeek 供应商 + 用户消息为续写意图
     *
     * @param command 流式对话命令
     * @param ctx     对话上下文
     * @return true 表示应走前缀续写管道
     */
    private boolean isPrefixContinueEligible(StreamChatCommand command, ChatContext ctx) {
        return !ctx.isNewConversation()
                && "deepseek".equals(ctx.providerCode())
                && ContinueIntentDetector.isContinueIntent(command.getMessage());
    }

    /**
     * 构建前缀续写管道
     * <p>
     * 绕过 ChatClient + MessageChatMemoryAdvisor，直接使用 ChatModel.stream(Prompt) 实现前缀续写。
     * 核心流程：
     * <ol>
     *   <li>从 ChatMemory 加载历史消息</li>
     *   <li>找到最后一条 ASSISTANT 消息作为 prefix</li>
     *   <li>移除该 ASSISTANT 消息，追加 DeepSeekAssistantMessage.prefixAssistantMessage(lastContent)</li>
     *   <li>直接调用 ChatModel.stream(Prompt)</li>
     *   <li>完成后手动将用户"继续"消息 + 续写内容保存到 ChatMemory</li>
     * </ol>
     *
     * @param command 流式对话命令
     * @param ctx     对话上下文
     * @return SSE 流
     */
    private Flux<ServerSentEvent<String>> buildPrefixContinuePipeline(StreamChatCommand command, ChatContext ctx) {
        AtomicReference<Usage> usageRef = new AtomicReference<>();
        AtomicReference<String> finishReasonRef = new AtomicReference<>();
        AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
        StringBuilder contentCollector = new StringBuilder();

        return Mono.fromCallable(() -> buildPrefixPrompt(ctx.conversationId()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(prompt -> ctx.chatModel().stream(prompt)
                        .doOnNext(chatResponse -> {
                            captureMetadata(chatResponse, usageRef, finishReasonRef);
                            collectText(chatResponse, contentCollector);
                        })
                        .flatMapIterable(chatResponse -> extractSseEvents(chatResponse, ctx.capReasoning()))
                        .concatWith(Flux.just(ServerSentEvent.<String>builder().event("done").data("[DONE]").build()))
                        .doOnComplete(() ->
                                Mono.fromRunnable(() -> onPrefixContinueComplete(command, ctx, contentCollector,
                                                usageRef, finishReasonRef, startTime))
                                        .subscribeOn(Schedulers.boundedElastic()).subscribe()
                        )
                        .doOnError(error -> {
                            log.error("前缀续写异常, conversationId={}", ctx.conversationId(), error);
                            Mono.fromRunnable(() -> recordUsageLog(
                                    command, ctx, usageRef.get(), "error",
                                    (int) (System.currentTimeMillis() - startTime.get()), false, error.getMessage()
                            )).subscribeOn(Schedulers.boundedElastic()).subscribe();
                        })
                );
    }

    /**
     * 从历史消息构建前缀续写 Prompt
     * <p>
     * 找到最后一条 ASSISTANT 消息，将其替换为 {@link DeepSeekAssistantMessage#prefixAssistantMessage(String)}，
     * 使 DeepSeek Beta 端点从该前缀处继续生成。
     *
     * @param conversationId 会话唯一标识
     * @return 构建好的 Prompt
     */
    private Prompt buildPrefixPrompt(String conversationId) {
        List<Message> history = chatMemory.get(conversationId);
        if (history.isEmpty()) {
            throw new BizException(AiErrorCode.PREFIX_CONTINUE_NO_HISTORY);
        }

        // 找到最后一条 ASSISTANT 消息
        String lastAssistantContent = null;
        int lastAssistantIndex = -1;
        for (int i = history.size() - 1; i >= 0; i--) {
            if (history.get(i) instanceof AssistantMessage am) {
                lastAssistantContent = am.getText();
                lastAssistantIndex = i;
                break;
            }
        }
        if (lastAssistantContent == null || lastAssistantContent.isBlank()) {
            throw new BizException(AiErrorCode.PREFIX_CONTINUE_NO_HISTORY);
        }

        // 构建消息列表：移除最后一条 ASSISTANT，追加 prefix 消息
        List<Message> messages = new ArrayList<>(history.subList(0, lastAssistantIndex));
        if (lastAssistantIndex + 1 < history.size()) {
            messages.addAll(history.subList(lastAssistantIndex + 1, history.size()));
        }
        messages.add(DeepSeekAssistantMessage.prefixAssistantMessage(lastAssistantContent));

        return new Prompt(messages);
    }

    /**
     * 从流式响应帧中收集文本内容到 StringBuilder
     *
     * @param chatResponse     流式响应帧
     * @param contentCollector 内容收集器
     */
    private void collectText(ChatResponse chatResponse, StringBuilder contentCollector) {
        String text = chatResponse.getResult().getOutput().getText();
        if (text != null && !text.isEmpty()) {
            contentCollector.append(text);
        }
    }

    /**
     * 前缀续写完成后的回调：持久化消息到 ChatMemory + 记录用量日志
     *
     * @param command          流式对话命令
     * @param ctx              对话上下文
     * @param contentCollector 续写内容收集器
     * @param usageRef         Usage 引用
     * @param finishReasonRef  finishReason 引用
     * @param startTime        开始时间
     */
    private void onPrefixContinueComplete(StreamChatCommand command, ChatContext ctx, StringBuilder contentCollector,
                                          AtomicReference<Usage> usageRef, AtomicReference<String> finishReasonRef,
                                          AtomicLong startTime) {
        try {
            chatMemory.add(ctx.conversationId(), List.of(
                    new UserMessage(command.getMessage()),
                    new AssistantMessage(contentCollector.toString())
            ));
        } catch (Exception e) {
            log.error("前缀续写消息持久化失败, conversationId={}", ctx.conversationId(), e);
        }

        recordUsageLog(command, ctx, usageRef.get(), finishReasonRef.get(), (int) (System.currentTimeMillis() - startTime.get()), true, null);
    }

    /**
     * 流式对话完成后的回调：记录用量日志 + 异步生成标题
     *
     * @param command      流式对话命令
     * @param ctx          对话上下文
     * @param usage        Token 用量
     * @param finishReason 结束原因
     * @param durationMs   耗时（毫秒）
     */
    private void onStreamComplete(StreamChatCommand command, ChatContext ctx, Usage usage, String finishReason, int durationMs) {
        recordUsageLog(command, ctx, usage, finishReason, durationMs, true, null);

        // 新建对话的首轮：异步生成 AI 标题
        if (ctx.isNewConversation()) {
            generateTitleAsync(ctx.conversationId(), ctx.chatModel(), command.getMessage());
        }
    }

    /**
     * 准备对话上下文：查找或创建会话、解析模型、解析系统提示词
     *
     * @param command 流式对话命令
     * @return 对话上下文
     */
    private ChatContext prepareChat(StreamChatCommand command) {
        String conversationId = command.getConversationId();
        Long modelId = command.getModelId();
        String systemPrompt = null;
        boolean isNewConversation = false;

        if (conversationId != null && !conversationId.isBlank()) {
            // 已有对话：校验存在 + 归属
            ConversationEntity conversation = requireConversation(conversationId);
            requireOwnership(conversation, command.getUserId());
            if (modelId == null) {
                modelId = conversation.getModelId();
            }
            systemPrompt = conversation.getSystemPrompt();
        } else {
            // 新建对话
            conversationId = UUID.randomUUID().toString();
            systemPrompt = resolveSystemPrompt(command);
            isNewConversation = true;

            ConversationEntity newConversation = new ConversationEntity();
            newConversation.setConversationId(conversationId);
            newConversation.setEstabId(command.getEstabId());
            newConversation.setUserId(command.getUserId());
            newConversation.setTitle(truncateTitle(command.getMessage()));
            newConversation.setModelId(modelId);
            newConversation.setSystemPrompt(systemPrompt);
            newConversation.setPinned(0);
            newConversation.setStatus(1);

            aiRepository.insertConversation(newConversation);
        }

        // 解析 ChatModel
        ChatModel chatModel = modelId != null
                ? chatModelRouter.resolve(command.getEstabId(), modelId)
                : chatModelRouter.resolveDefault(command.getEstabId());

        // 解析 capReasoning 和 providerCode
        boolean capReasoning = false;
        String providerCode = null;
        if (modelId != null) {
            ModelEntity modelEntity = aiRepository.findModelById(modelId);
            if (modelEntity != null) {
                capReasoning = modelEntity.getCapReasoning() != null && modelEntity.getCapReasoning() == 1;
                ProviderEntity provider = aiRepository.findProviderById(modelEntity.getProviderId());
                if (provider != null) {
                    providerCode = provider.getProviderCode();
                }
            }
        }

        return new ChatContext(conversationId, modelId, systemPrompt, chatModel, isNewConversation, capReasoning, providerCode);
    }

    /**
     * 截取消息作为临时标题（AI 标题生成前的占位）
     *
     * @param message 用户消息
     * @return 截取后的标题
     */
    private String truncateTitle(String message) {
        return message.length() > 50 ? message.substring(0, 50) + "..." : message;
    }

    /**
     * 异步调用 AI 生成对话标题并回写数据库
     * <p>
     * 使用内建的提示词模板，通过 Spring AI PromptTemplate 渲染后同步调用模型生成标题。
     * 生成完成后更新 ai_conversation.title。
     * <p>
     * 前端标题刷新策略：SSE 流已关闭无法追加事件，前端通过以下方式获取更新后的标题：
     * <ul>
     *   <li>方案一（当前）：前端在收到 [DONE] 事件后延迟几秒调用 GET /conversations 刷新列表</li>
     *   <li>方案二（后续）：升级为 WebSocket 推送 title_updated 事件</li>
     * </ul>
     *
     * @param conversationId 会话唯一标识
     * @param chatModel      ChatModel 实例
     * @param userMessage    用户首条消息
     */
    private void generateTitleAsync(String conversationId, ChatModel chatModel, String userMessage) {
        Mono.fromRunnable(() -> {
            try {
                PromptTemplate promptTemplate = PromptTemplate.builder()
                        .template(TITLE_GENERATION_PROMPT)
                        .build();
                String renderedPrompt = promptTemplate.render(Map.of("message", userMessage));

                String generatedTitle = ChatClient.builder(chatModel)
                        .build()
                        .prompt()
                        .user(renderedPrompt)
                        .call()
                        .content();

                if (generatedTitle != null && !generatedTitle.isBlank()) {
                    generatedTitle = cleanTitle(generatedTitle);

                    ConversationEntity conversation = aiRepository.findConversationByConversationId(conversationId);
                    if (conversation != null) {
                        conversation.setTitle(generatedTitle);
                        aiRepository.updateConversation(conversation);
                        log.debug("AI 生成对话标题成功, conversationId={}, title={}", conversationId, generatedTitle);
                    }
                }
            } catch (Exception e) {
                log.warn("AI 生成对话标题失败, conversationId={}", conversationId, e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    /**
     * 清理 AI 生成的标题（去除引号、多余空白、截断超长）
     *
     * @param title AI 生成的原始标题
     * @return 清理后的标题
     */
    private String cleanTitle(String title) {
        String cleaned = title.trim()
                .replaceAll("^[\"'\\u201c\\u201d\\u2018\\u2019]+|[\"'\\u201c\\u201d\\u2018\\u2019]+$", "")
                .trim();
        if (cleaned.length() > 255) {
            cleaned = cleaned.substring(0, 252) + "...";
        }
        return cleaned;
    }

    /**
     * 解析系统提示词（模板渲染 or 直传）
     * <p>
     * 优先级：promptTemplateId > systemPrompt（直传）> null
     *
     * @param command 流式对话命令
     * @return 系统提示词，可能为 null
     */
    private String resolveSystemPrompt(StreamChatCommand command) {
        if (command.getPromptTemplateId() != null) {
            PromptTemplateEntity template = aiRepository.findPromptTemplateById(command.getPromptTemplateId());
            if (template == null) {
                throw new BizException(AiErrorCode.PROMPT_TEMPLATE_NOT_FOUND);
            }

            try {
                Map<String, Object> variables = command.getTemplateVariables() != null
                        ? Map.copyOf(command.getTemplateVariables())
                        : Map.of();

                String varOpen = template.getVarOpen() != null ? template.getVarOpen() : "{{";
                String varClose = template.getVarClose() != null ? template.getVarClose() : "}}";

                PromptTemplate promptTemplate = PromptTemplate.builder()
                        .renderer(StTemplateRenderer.builder()
                                .startDelimiterToken(varOpen.charAt(0))
                                .endDelimiterToken(varClose.charAt(0))
                                .build())
                        .template(template.getContent())
                        .build();

                return promptTemplate.render(variables);
            } catch (Exception e) {
                log.error("Prompt模板渲染失败, templateId={}", command.getPromptTemplateId(), e);
                throw new BizException(AiErrorCode.PROMPT_TEMPLATE_RENDER_ERROR);
            }
        }

        return command.getSystemPrompt();
    }

    /**
     * 构建 ChatClient（挂载 ChatMemory Advisor 和 Logger Advisor）
     *
     * @param chatModel    ChatModel 实例
     * @param systemPrompt 系统提示词（可为 null）
     * @return ChatClient 实例
     */
    private ChatClient buildChatClient(ChatModel chatModel, String systemPrompt) {
        ChatClient.Builder builder = ChatClient.builder(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        SimpleLoggerAdvisor.builder().build()
                );

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            builder.defaultSystem(systemPrompt);
        }

        return builder.build();
    }

    /**
     * 记录 AI 调用日志
     *
     * @param command      流式对话命令
     * @param ctx          对话上下文
     * @param usage        Token 用量（可为 null）
     * @param finishReason 结束原因
     * @param durationMs   耗时（毫秒）
     * @param success      是否成功
     * @param errorMessage 错误信息
     */
    private void recordUsageLog(StreamChatCommand command, ChatContext ctx, Usage usage, String finishReason, int durationMs, boolean success, String errorMessage) {
        try {
            UsageLogEntity usageLog = new UsageLogEntity();
            usageLog.setEstabId(command.getEstabId());
            usageLog.setUserId(command.getUserId());
            usageLog.setConversationId(ctx.conversationId());
            usageLog.setModelId(ctx.modelId());
            usageLog.setRequestType(RequestType.CHAT.getCode());
            usageLog.setDurationMs(durationMs);
            usageLog.setFinishReason(finishReason);
            usageLog.setSuccess(success ? 1 : 0);
            usageLog.setErrorMessage(errorMessage);

            if (usage != null) {
                usageLog.setInputTokens(usage.getPromptTokens());
                usageLog.setOutputTokens(usage.getCompletionTokens());
                usageLog.setTotalTokens(usage.getTotalTokens());
            }

            usageLog.setTotalCost(calculateCost(ctx.modelId(), usage));

            aiRepository.insertUsageLog(usageLog);
        } catch (Exception e) {
            log.error("记录调用日志失败, conversationId={}", ctx.conversationId(), e);
        }
    }

    /**
     * 根据模型定价和 token 用量计算本次调用费用
     * <p>
     * 计算公式：total_cost = (inputTokens * inputPrice + outputTokens * outputPrice) / 1_000_000
     * 价格单位为 "每百万 token / 美元"。
     *
     * @param modelId 模型ID
     * @param usage   Token 用量（可为 null）
     * @return 本次调用费用（美元），无法计算时返回 null
     */
    private BigDecimal calculateCost(Long modelId, Usage usage) {
        if (modelId == null || usage == null) {
            return null;
        }

        ModelEntity model = aiRepository.findModelById(modelId);
        if (model == null || model.getInputPrice() == null || model.getOutputPrice() == null) {
            return null;
        }

        Integer inputTokens = usage.getPromptTokens();
        Integer outputTokens = usage.getCompletionTokens();
        if (inputTokens == null && outputTokens == null) {
            return null;
        }

        BigDecimal inputCost = BigDecimal.ZERO;
        if (inputTokens != null) {
            inputCost = model.getInputPrice()
                    .multiply(BigDecimal.valueOf(inputTokens))
                    .divide(PER_MILLION, 6, RoundingMode.HALF_UP);
        }

        BigDecimal outputCost = BigDecimal.ZERO;
        if (outputTokens != null) {
            outputCost = model.getOutputPrice()
                    .multiply(BigDecimal.valueOf(outputTokens))
                    .divide(PER_MILLION, 6, RoundingMode.HALF_UP);
        }

        return inputCost.add(outputCost);
    }

    /**
     * 查询对话并校验存在
     *
     * @param conversationId 会话唯一标识
     * @return 对话实体
     */
    private ConversationEntity requireConversation(String conversationId) {
        ConversationEntity conversation = aiRepository.findConversationByConversationId(conversationId);
        if (conversation == null) {
            throw new BizException(AiErrorCode.CONVERSATION_NOT_FOUND);
        }
        return conversation;
    }

    /**
     * 校验对话归属权
     *
     * @param conversation 对话实体
     * @param userId       当前用户ID
     */
    private void requireOwnership(ConversationEntity conversation, Long userId) {
        if (!conversation.getUserId().equals(userId)) {
            throw new BizException(AiErrorCode.CONVERSATION_NOT_OWNED);
        }
    }

    /**
     * 对话上下文（内部传递用）
     *
     * @param conversationId    会话唯一标识
     * @param modelId           模型ID
     * @param systemPrompt      系统提示词
     * @param chatModel         ChatModel 实例
     * @param isNewConversation 是否为新建对话
     * @param capReasoning      是否支持深度推理
     * @param providerCode      供应商编码
     */
    private record ChatContext(
            String conversationId,
            Long modelId,
            String systemPrompt,
            ChatModel chatModel,
            boolean isNewConversation,
            boolean capReasoning,
            String providerCode
    ) {
    }
}
