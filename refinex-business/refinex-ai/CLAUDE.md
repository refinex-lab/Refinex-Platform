# CLAUDE.md — refinex-ai

AI 与 Agent 智能服务，基于 Spring AI 1.1.2 构建。

## 服务概要

| 项      | 值                                               |
|--------|-------------------------------------------------|
| 服务名    | `refinex-ai`                                    |
| 端口     | 8084                                            |
| 主类     | `cn.refinex.ai.RefinexAiApplication`            |
| HTTP 栈 | WebFlux（非 Servlet）                              |
| AI 框架  | Spring AI 1.1.2                                 |
| ORM    | MyBatis-Plus 3.5.15（阻塞 JDBC，与 WebFlux HTTP 层共存） |
| 数据库    | MySQL 8+（业务表 + Spring AI ChatMemory 表）          |

## 技术栈特殊说明

### WebFlux + JDBC 混合架构

本服务 HTTP 层使用 WebFlux（Netty），但持久化层仍使用阻塞式 JDBC（MyBatis-Plus + Druid）。这是有意为之的架构选择：

- WebFlux 负责 HTTP 请求/响应的非阻塞处理，特别是 SSE 流式输出
- JDBC 操作在 `Schedulers.boundedElastic()` 上执行，不阻塞 Netty EventLoop
- Spring AI 的 `JdbcChatMemoryRepository` 也是阻塞式 JDBC，同理

### refinex-web 排除 spring-boot-starter-web

`refinex-web` 传递依赖 `spring-boot-starter-web`（Servlet/Tomcat），已在 pom.xml 中排除。排除后：

- `Result`、`PageResult`、`UserContext`、`TokenUtils` 等工具类仍可用
- `TokenFilter`（Servlet Filter）和 `GlobalExceptionHandler`（`@ControllerAdvice`）不生效
- 已在 `infrastructure/config/` 下实现 WebFlux 版本替代（见下方）

### WebFlux 基础设施组件（infrastructure/config/）

本服务已实现三个 WebFlux 专属配置类，替代 `refinex-web` 中基于 Servlet 的对应组件：

| 本服务类                             | 替代的 refinex-web 类             | 说明                                                                                 |
|----------------------------------|-------------------------------|------------------------------------------------------------------------------------|
| `ReactiveTokenFilter`            | `TokenFilter`                 | 实现 `WebFilter`（非 Servlet `Filter`），阻塞操作（Redis Lua、Sa-Token）调度到 `boundedElastic` 线程 |
| `ReactiveGlobalExceptionHandler` | `GlobalExceptionHandler`      | 参数校验异常捕获 `WebExchangeBindException`（非 `MethodArgumentNotValidException`），其余逻辑一致    |
| `AiWebFluxConfiguration`         | `RefinexWebAutoConfiguration` | 注册上述两个 Bean，复用 `RefinexWebProperties` 配置                                           |

关键设计决策：

- `ReactiveTokenFilter` 的校验逻辑（防重放 Token + en 登录态）与 Servlet 版完全对齐，确保网关透传的 Token 在 AI 服务中同样有效
- `UserContext`（ThreadLocal）在 `boundedElastic` 线程上设置，后续阻塞的 JDBC 操作也在同一线程池，ThreadLocal 传递无问题
- `ReactiveGlobalExceptionHandler` 使用 `@RestControllerAdvice`，WebFlux 原生支持，无需额外适配
- 配置开关复用 `refinex.web.enabled=true` 和 `refinex.web.exclude-urls`，与其他服务保持一致

## 包结构（DDD 分层）

```
cn.refinex.ai/
├── RefinexAiApplication.java
│
├── interfaces/                        # 接口层（Inbound）
│   ├── controller/                    #   REST Controller（返回 Mono/Flux）
│   ├── dto/                           #   *Request（写）、*Query（读）入参
│   ├── vo/                            #   *VO 出参
│   └── assembler/                     #   *ApiAssembler（VO/Request ↔ DTO/Command）
│
├── application/                       # 应用层
│   ├── service/                       #   *ApplicationService 用例编排
│   ├── command/                       #   *Command 写操作 + 查询参数
│   ├── dto/                           #   *DTO 应用层数据传输
│   └── assembler/                     #   *DomainAssembler（DTO ↔ Entity）
│
├── domain/                            # 领域层
│   ├── model/
│   │   ├── entity/                    #   *Entity 领域实体
│   │   └── enums/                     #   领域枚举（ModelType、ToolType 等）
│   ├── repository/                    #   *Repository 仓储接口
│   └── error/                         #   *ErrorCode 领域错误码
│
└── infrastructure/                    # 基础设施层（Outbound）
    ├── config/                        #   WebFlux 异常处理器、Spring AI 配置、ChatClient Bean
    ├── converter/                     #   *DoConverter（MapStruct DO ↔ Entity）
    ├── persistence/
    │   ├── dataobject/                #   *Do 数据库映射对象
    │   ├── mapper/                    #   *Mapper MyBatis-Plus
    │   └── repository/               #   *RepositoryImpl 仓储实现
    └── client/                        #   外部服务客户端（调用 refinex-user 等）
```

## 数据库表

Schema 文件：`document/sql/003_ai_schema.sql`，种子数据：`document/sql/004_ai_seed.sql`

### AI 模型与供应商

| 表                    | 说明                                                       |
|----------------------|----------------------------------------------------------|
| `ai_provider`        | AI 供应商（OpenAI、Anthropic、DeepSeek 等）                      |
| `ai_model`           | 模型目录，含能力标签（vision/tool_call/structured_output/streaming） |
| `ai_model_provision` | 租户模型开通，存储加密 API Key 和用量配额                                |

### Prompt 与对话

| 表                       | 说明                                                   |
|-------------------------|------------------------------------------------------|
| `ai_prompt_template`    | Prompt 模板，支持自定义变量界定符（`var_open`/`var_close`）         |
| `ai_conversation`       | 对话元数据，`conversation_id`（UUID）关联 Spring AI ChatMemory |
| `SPRING_AI_CHAT_MEMORY` | Spring AI 1.1.2 官方标准表，框架自动读写，勿修改结构                   |

### Tool / MCP / Skill

| 表                    | 说明                            |
|----------------------|-------------------------------|
| `ai_tool`            | 工具注册（FUNCTION/MCP/HTTP 三种类型）  |
| `ai_mcp_server`      | MCP Server 连接管理（stdio/sse）    |
| `ai_skill`           | 技能编排 = 模型 + Prompt + 工具 + 知识库 |
| `ai_skill_tool`      | 技能-工具关联（多对多）                  |
| `ai_skill_knowledge` | 技能-知识库关联，含独立 RAG 检索参数         |

### 知识库（refinex-kb 共用）

| 表                   | 说明                                     |
|---------------------|----------------------------------------|
| `kb_knowledge_base` | 知识库定义，`vectorized` 控制是否开启向量化           |
| `kb_folder`         | 目录树（parent_id 无限层级）                    |
| `kb_document`       | 文档（MD/PDF/DOCX/XLSX/PPTX/TXT/HTML/CSV） |
| `kb_document_chunk` | 文档切片，`embedding_id` 关联外部向量数据库          |

### 用量

| 表              | 说明                      |
|----------------|-------------------------|
| `ai_usage_log` | AI 调用日志（token 用量、费用、耗时） |

## Spring AI 核心概念映射

| Spring AI 概念               | 本服务对应                                                                                 |
|----------------------------|---------------------------------------------------------------------------------------|
| `ChatModel`                | 多个 starter 自动注入（OpenAI/Anthropic/DeepSeek/ZhiPu/MiniMax），通过 `ai_model_provision` 动态路由 |
| `ChatClient`               | 在 `infrastructure/config/` 中构建，挂载 Advisors                                            |
| `ChatMemory`               | `JdbcChatMemoryRepository` → `SPRING_AI_CHAT_MEMORY` 表                                |
| `MessageChatMemoryAdvisor` | 自动加载/保存对话历史                                                                           |
| `QuestionAnswerAdvisor`    | RAG 问答，从 VectorStore 检索后增强 Prompt                                                     |
| `ToolCallback`             | `ai_tool` 表注册的工具，运行时转为 Spring AI ToolCallback                                         |
| `McpSyncClient`            | `ai_mcp_server` 表注册的 MCP Server，运行时建立连接                                               |
| `DocumentReader`           | Tika 文档读取器，解析知识库上传的 PDF/DOCX 等文件                                                      |
| Structured Output          | 通过 `ChatClient.call().entity(Class)` 实现，依赖模型 `cap_structured_output` 能力               |

## 构建与运行

```bash
# 从项目根目录构建
cd Refinex-Platform
mvn clean install -DskipTests

# 单独构建 AI 模块
cd refinex-business/refinex-ai
mvn clean package -DskipTests

# 启动（需要 Nacos、MySQL、Redis 已就绪）
mvn spring-boot:run
```

启动顺序：Nacos → MySQL → Redis → Gateway(8080) → Auth(8081) → **AI(8084)**

## 配置管理

本地 `application.yml` 仅负责连接 Nacos，业务配置全部在 Nacos 远程管理：

| Nacos DataId             | Group             | 说明                                |
|--------------------------|-------------------|-----------------------------------|
| `refinex-base.yml`       | `REFINEX_SHARED`  | 共享基础参数                            |
| `refinex-cache.yml`      | `REFINEX_SHARED`  | Redis 配置                          |
| `refinex-datasource.yml` | `REFINEX_SHARED`  | 数据源配置                             |
| `refinex-ai.yml`         | `REFINEX_SERVICE` | AI 服务专属（模型 API Key、Spring AI 参数等） |

敏感配置（API Key、数据库密码）放在 `config/application.yml`（不入 Git）。

## 命名约定

与其他业务模块一致：

| 类型                  | 命名                    | 示例                               |
|---------------------|-----------------------|----------------------------------|
| Controller          | `*Controller`         | `ConversationController`         |
| Application Service | `*ApplicationService` | `ConversationApplicationService` |
| Command             | `*Command`            | `CreateConversationCommand`      |
| DTO                 | `*DTO`                | `ConversationDTO`                |
| Request             | `*Request`            | `ConversationCreateRequest`      |
| Query               | `*Query`              | `ConversationListQuery`          |
| VO                  | `*VO`                 | `ConversationVO`                 |
| Entity              | `*Entity`             | `ConversationEntity`             |
| DO                  | `*Do`                 | `AiConversationDo`               |
| Mapper              | `*Mapper`             | `AiConversationMapper`           |
| Repository 接口       | `*Repository`         | `ConversationRepository`         |
| Repository 实现       | `*RepositoryImpl`     | `ConversationRepositoryImpl`     |
| DO ↔ Entity 转换      | `*DoConverter`        | `ConversationDoConverter`        |
| API 层转换             | `*ApiAssembler`       | `ConversationApiAssembler`       |
| Domain 层转换          | `*DomainAssembler`    | `AiDomainAssembler`              |
| ErrorCode           | `*ErrorCode`          | `AiErrorCode`                    |

## 编码规范（强制）

### 字段注释对齐数据库 COMMENT

所有 Java POJO 类（DO、Entity、DTO、VO、Command、Query、Request）的每个字段注释必须严格对齐 `document/sql/` 下对应数据库表字段的
COMMENT，保持文字完全一致，不得自行改写或省略。

数据库 COMMENT 是字段注释的唯一权威来源。示例：

```sql
-- 数据库定义
model_type
TINYINT NOT NULL DEFAULT 1 COMMENT '模型类型 1聊天 2嵌入 3图像生成 4语音转文字 5文字转语音 6重排序 7内容审核',
```

```java
// Java 字段注释（必须与上方 COMMENT 完全一致）
/**
 * 模型类型 1聊天 2嵌入 3图像生成 4语音转文字 5文字转语音 6重排序 7内容审核
 */
private Integer modelType;
```

### 方法注释规范

所有公开方法（public method）必须编写标准 Javadoc 注释，包含：

- 方法功能说明
- `@param` 每个参数的描述
- `@return` 返回值描述（void 方法除外）

私有辅助方法也应有简要 Javadoc（至少包含功能说明和 `@param`、`@return`）。

## WebFlux 开发注意事项

1. Controller 方法返回 `Mono<Result<T>>` 或 `Flux<T>`（SSE 流式场景）
2. 不要在 Netty EventLoop 线程上执行阻塞操作（JDBC、文件 IO）
3. 阻塞操作用 `Mono.fromCallable(() -> ...).subscribeOn(Schedulers.boundedElastic())` 包装
4. SSE 流式对话端点使用 `@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)` 或 `Flux<ServerSentEvent<T>>`
5. WebFlux 异常处理用 `@ControllerAdvice` + `@ExceptionHandler`（WebFlux 也支持），或实现 `WebExceptionHandler`
6. Token 认证过滤器实现 `WebFilter` 接口，而非 Servlet `Filter`

## 多租户隔离

- 所有业务表通过 `estab_id` 隔离
- 平台级资源 `estab_id = 0`（如内置 Prompt 模板、内置工具）
- `ai_model_provision` 按租户存储独立 API Key
- 查询时始终带 `estab_id` 条件
