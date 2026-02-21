-- ============================================================================
-- Refinex Platform AI 与知识库模块 MySQL 建表结构
-- 约定：
-- 1) 所有表包含 BaseEntity 通用字段：id, create_by, update_by, delete_by, deleted, lock_version, gmt_create, gmt_modified
-- 2) 编码字段统一使用 code 后缀，排序字段统一为 sort，状态字段统一为 status
-- 3) 字符集/排序规则统一：utf8mb4 / utf8mb4_0900_ai_ci
-- 4) 多租户隔离字段：estab_id（平台级为 0）
-- 5) 本脚本依赖 001_user_rbac_schema.sql 已执行完成
-- ============================================================================

USE refinex_platform;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================
-- AI 模型与供应商管理
-- ============================

-- AI 供应商定义
-- 设计理由：将供应商（如 OpenAI、Anthropic、DeepSeek）抽象为独立实体，
-- 与具体模型解耦，便于统一管理 API 基础地址、协议类型等供应商级别属性。
-- 一个供应商下可挂载多个模型。
DROP TABLE IF EXISTS ai_provider;
CREATE TABLE ai_provider (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  provider_code VARCHAR(64) NOT NULL COMMENT '供应商编码(如 openai/anthropic/deepseek/zhipu/minimax)',
  provider_name VARCHAR(128) NOT NULL COMMENT '供应商名称',
  protocol VARCHAR(32) NOT NULL DEFAULT 'openai' COMMENT '接口协议(openai/anthropic/ollama)',
  base_url VARCHAR(255) DEFAULT NULL COMMENT '默认 API 基础地址',
  icon_url VARCHAR(255) DEFAULT NULL COMMENT '供应商图标地址',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_provider_code (provider_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI供应商定义';

-- AI 模型定义
-- 设计理由：模型是 AI 能力的最小调用单元。同一供应商下有多个模型（如 gpt-4o / gpt-4o-mini），
-- 每个模型有独立的能力标签（是否支持视觉、工具调用、结构化输出等），
-- 这些能力标签直接决定前端 UI 展示和后端路由逻辑。
-- max_tokens / max_context_window 用于前端输入限制和后端 token 预算计算。
DROP TABLE IF EXISTS ai_model;
CREATE TABLE ai_model (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  provider_id BIGINT NOT NULL COMMENT '供应商ID',
  model_code VARCHAR(128) NOT NULL COMMENT '模型编码(API 调用时的 model 参数值，如 gpt-4o)',
  model_name VARCHAR(128) NOT NULL COMMENT '模型显示名称',
  model_type TINYINT NOT NULL DEFAULT 1 COMMENT '模型类型 1聊天 2嵌入 3图像生成 4语音转文字 5文字转语音 6重排序',
  cap_vision TINYINT NOT NULL DEFAULT 0 COMMENT '能力:视觉理解 1支持 0不支持',
  cap_tool_call TINYINT NOT NULL DEFAULT 0 COMMENT '能力:工具调用 1支持 0不支持',
  cap_structured_output TINYINT NOT NULL DEFAULT 0 COMMENT '能力:结构化输出 1支持 0不支持',
  cap_streaming TINYINT NOT NULL DEFAULT 1 COMMENT '能力:流式输出 1支持 0不支持',
  max_context_window INT DEFAULT NULL COMMENT '最大上下文窗口(token数)',
  max_output_tokens INT DEFAULT NULL COMMENT '最大输出token数',
  input_price DECIMAL(12,6) DEFAULT NULL COMMENT '输入价格(每百万token/美元)',
  output_price DECIMAL(12,6) DEFAULT NULL COMMENT '输出价格(每百万token/美元)',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息(如特殊参数默认值)',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_model_code (provider_id, model_code),
  KEY idx_model_type (model_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI模型定义';

-- 租户模型开通配置
-- 设计理由：平台定义了全局的 provider + model 目录后，每个租户需要独立开通并配置自己的 API Key。
-- 这张表实现了「平台统一模型目录 + 租户独立凭证」的分层架构。
-- api_key 存储加密后的密文，api_base_url 允许租户覆盖供应商默认地址（如私有化部署场景）。
-- daily_quota / monthly_quota 用于租户级别的用量管控。
DROP TABLE IF EXISTS ai_model_provision;
CREATE TABLE ai_model_provision (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  estab_id BIGINT NOT NULL COMMENT '组织ID',
  model_id BIGINT NOT NULL COMMENT '模型ID',
  api_key_cipher TEXT DEFAULT NULL COMMENT 'API Key密文(AES加密存储)',
  api_base_url VARCHAR(255) DEFAULT NULL COMMENT '自定义API地址(覆盖供应商默认)',
  daily_quota INT DEFAULT NULL COMMENT '日调用额度(NULL不限)',
  monthly_quota INT DEFAULT NULL COMMENT '月调用额度(NULL不限)',
  is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否该租户默认模型 1是 0否',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息(如自定义请求头)',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_estab_model (estab_id, model_id),
  KEY idx_estab_default (estab_id, is_default, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租户模型开通配置';

-- ============================
-- Prompt 模板管理
-- ============================

-- Prompt 模板
-- 设计理由：Prompt 是 AI 应用的核心资产，需要版本化管理。
-- 模板支持变量占位符（如 {{context}}、{{question}}），运行时由应用层渲染。
-- category 用于分类检索（如 RAG问答、摘要、翻译、代码生成等）。
-- 同一 prompt 可被多个 Agent/会话复用，因此独立建表而非内嵌到会话中。
DROP TABLE IF EXISTS ai_prompt_template;
CREATE TABLE ai_prompt_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  estab_id BIGINT NOT NULL DEFAULT 0 COMMENT '组织ID(平台级为0)',
  prompt_code VARCHAR(64) NOT NULL COMMENT '模板编码',
  prompt_name VARCHAR(128) NOT NULL COMMENT '模板名称',
  category VARCHAR(64) DEFAULT NULL COMMENT '分类(如 rag/summary/translate/code_gen/chat)',
  content MEDIUMTEXT NOT NULL COMMENT '模板内容(变量占位符格式: 开始界定符+变量名+结束界定符)',
  variables JSON DEFAULT NULL COMMENT '变量定义([{"name":"context","desc":"上下文","required":true}])',
  var_open VARCHAR(8) NOT NULL DEFAULT '{{' COMMENT '变量占位符开始界定符(默认 {{)',
  var_close VARCHAR(8) NOT NULL DEFAULT '}}' COMMENT '变量占位符结束界定符(默认 }})',
  language VARCHAR(16) NOT NULL DEFAULT 'zh-CN' COMMENT '语言',
  is_builtin TINYINT NOT NULL DEFAULT 0 COMMENT '是否内置 1是 0否',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_prompt_code (estab_id, prompt_code),
  KEY idx_prompt_category (category, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Prompt模板';

-- ============================
-- 对话与聊天记忆
-- ============================

-- AI 对话（会话）
-- 设计理由：对话是用户与 AI 交互的顶层容器，对应前端一个聊天窗口。
-- 每个对话绑定一个模型（model_id）和可选的 system prompt，
-- conversation_id 是 UUID，同时作为 Spring AI ChatMemory 的 conversationId 传递，
-- 实现业务对话元数据与 Spring AI 消息存储的关联。
-- pinned 支持用户置顶常用对话。
DROP TABLE IF EXISTS ai_conversation;
CREATE TABLE ai_conversation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  conversation_id VARCHAR(36) NOT NULL COMMENT '会话唯一标识(UUID，关联 Spring AI ChatMemory)',
  estab_id BIGINT NOT NULL COMMENT '组织ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  title VARCHAR(255) DEFAULT NULL COMMENT '对话标题(可由 AI 自动生成)',
  model_id BIGINT DEFAULT NULL COMMENT '使用的模型ID',
  system_prompt TEXT DEFAULT NULL COMMENT '系统提示词(本次对话的 system message)',
  pinned TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶 1是 0否',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1进行中 2已归档',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息(如温度、top_p等对话级参数覆盖)',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_conversation_id (conversation_id),
  KEY idx_conv_user (estab_id, user_id, deleted, gmt_modified),
  KEY idx_conv_pinned (estab_id, user_id, pinned, gmt_modified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI对话';

-- Spring AI 聊天记忆（JDBC 持久化）
-- 设计理由：这是 Spring AI 1.1.2 JdbcChatMemoryRepository 要求的标准表结构。
-- Spring AI 框架会自动读写此表，我们不做任何字段修改以保持兼容性。
-- conversation_id 与 ai_conversation.conversation_id 逻辑关联（非外键，因为此表由框架管理）。
-- 注意：此表无 BaseEntity 字段，完全遵循 Spring AI 官方 schema。
DROP TABLE IF EXISTS SPRING_AI_CHAT_MEMORY;
CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
  `conversation_id` VARCHAR(36) NOT NULL,
  `content` TEXT NOT NULL,
  `type` ENUM('USER', 'ASSISTANT', 'SYSTEM', 'TOOL') NOT NULL,
  `timestamp` TIMESTAMP NOT NULL,
  INDEX `SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX` (`conversation_id`, `timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Spring AI聊天记忆(框架标准表)';

-- ============================
-- Tool / MCP / Skill 管理
-- ============================

-- AI 工具定义
-- 设计理由：Spring AI Tool Calling 和 MCP 都需要工具注册。
-- 此表统一管理平台内所有可被 AI 调用的工具，包括：
-- 1) 内置 Java 方法工具（tool_type=FUNCTION）：对应 @Tool 注解的 Spring Bean 方法
-- 2) MCP 服务器工具（tool_type=MCP）：对应外部 MCP Server 暴露的工具
-- 3) HTTP API 工具（tool_type=HTTP）：对应外部 REST API 封装
-- input_schema / output_schema 存储 JSON Schema，供 AI 模型理解工具的输入输出格式。
-- 安全考量：require_confirm 标记高风险工具（如删除数据），需用户二次确认后才执行。
DROP TABLE IF EXISTS ai_tool;
CREATE TABLE ai_tool (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  estab_id BIGINT NOT NULL DEFAULT 0 COMMENT '组织ID(平台级为0)',
  tool_code VARCHAR(128) NOT NULL COMMENT '工具编码(全局唯一标识)',
  tool_name VARCHAR(128) NOT NULL COMMENT '工具显示名称',
  tool_type VARCHAR(32) NOT NULL DEFAULT 'FUNCTION' COMMENT '工具类型(FUNCTION/MCP/HTTP)',
  description VARCHAR(512) NOT NULL COMMENT '工具描述(供 AI 模型理解用途)',
  input_schema JSON DEFAULT NULL COMMENT '输入参数 JSON Schema',
  output_schema JSON DEFAULT NULL COMMENT '输出结果 JSON Schema',
  handler_ref VARCHAR(255) DEFAULT NULL COMMENT '处理器引用(FUNCTION:Spring Bean名; MCP:server_id; HTTP:endpoint URL)',
  require_confirm TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要用户确认后执行 1是 0否',
  is_builtin TINYINT NOT NULL DEFAULT 0 COMMENT '是否内置 1是 0否',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息(如超时配置、重试策略)',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_tool_code (estab_id, tool_code),
  KEY idx_tool_type (tool_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI工具定义';

-- MCP 服务器注册
-- 设计理由：MCP（Model Context Protocol）是 AI 生态的标准化工具协议。
-- 一个 MCP Server 可暴露多个工具，此表管理 MCP Server 的连接信息。
-- transport_type 区分 stdio（本地进程）和 sse（远程 HTTP SSE）两种传输方式。
-- 服务器注册后，其暴露的工具会同步到 ai_tool 表（tool_type=MCP）。
DROP TABLE IF EXISTS ai_mcp_server;
CREATE TABLE ai_mcp_server (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  estab_id BIGINT NOT NULL DEFAULT 0 COMMENT '组织ID(平台级为0)',
  server_code VARCHAR(64) NOT NULL COMMENT '服务器编码',
  server_name VARCHAR(128) NOT NULL COMMENT '服务器名称',
  transport_type VARCHAR(16) NOT NULL DEFAULT 'sse' COMMENT '传输类型(stdio/sse)',
  endpoint_url VARCHAR(255) DEFAULT NULL COMMENT 'SSE 端点地址',
  command VARCHAR(255) DEFAULT NULL COMMENT 'stdio 启动命令',
  args JSON DEFAULT NULL COMMENT 'stdio 启动参数',
  env_vars JSON DEFAULT NULL COMMENT '环境变量(加密存储敏感值)',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_mcp_server_code (estab_id, server_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='MCP服务器注册';

-- Skill 定义（Agent 技能编排）
-- 设计理由：Skill 是面向用户的高层能力抽象，一个 Skill 编排了：
-- 1) 使用哪个模型（model_id）
-- 2) 使用哪个 system prompt（prompt_template_id）
-- 3) 挂载哪些工具（通过 ai_skill_tool 关联）
-- 4) 是否启用 RAG（关联知识库）
-- 这是构建 Agent 的基础单元。用户在前端选择 Skill 即可获得特定领域的 AI 能力，
-- 而无需关心底层模型和工具的组合细节。
DROP TABLE IF EXISTS ai_skill;
CREATE TABLE ai_skill (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  estab_id BIGINT NOT NULL DEFAULT 0 COMMENT '组织ID(平台级为0)',
  skill_code VARCHAR(64) NOT NULL COMMENT '技能编码',
  skill_name VARCHAR(128) NOT NULL COMMENT '技能名称',
  description VARCHAR(512) DEFAULT NULL COMMENT '技能描述',
  icon VARCHAR(64) DEFAULT NULL COMMENT '图标',
  model_id BIGINT DEFAULT NULL COMMENT '推荐使用的模型ID(可被用户覆盖)',
  prompt_template_id BIGINT DEFAULT NULL COMMENT '关联的Prompt模板ID',
  temperature DECIMAL(3,2) DEFAULT NULL COMMENT '温度参数(0.00-2.00)',
  top_p DECIMAL(3,2) DEFAULT NULL COMMENT 'Top P参数(0.00-1.00)',
  max_tokens INT DEFAULT NULL COMMENT '最大输出token数',
  is_builtin TINYINT NOT NULL DEFAULT 0 COMMENT '是否内置 1是 0否',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息(如 Advisor 链配置)',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_skill_code (estab_id, skill_code),
  KEY idx_skill_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI技能定义';

-- Skill-Tool 关联
-- 设计理由：一个 Skill 可挂载多个工具，一个工具也可被多个 Skill 复用，经典多对多关系。
DROP TABLE IF EXISTS ai_skill_tool;
CREATE TABLE ai_skill_tool (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  skill_id BIGINT NOT NULL COMMENT '技能ID',
  tool_id BIGINT NOT NULL COMMENT '工具ID',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_skill_tool (skill_id, tool_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='技能-工具关联';

-- Skill-知识库 关联
-- 设计理由：Skill 启用 RAG 时需要关联一个或多个知识库作为检索源。
-- top_k / similarity_threshold 允许每个关联独立配置检索参数，
-- 因为不同知识库的文档密度和质量不同，需要差异化调优。
DROP TABLE IF EXISTS ai_skill_knowledge;
CREATE TABLE ai_skill_knowledge (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  skill_id BIGINT NOT NULL COMMENT '技能ID',
  knowledge_base_id BIGINT NOT NULL COMMENT '知识库ID',
  top_k INT NOT NULL DEFAULT 4 COMMENT 'RAG检索返回文档数',
  similarity_threshold DECIMAL(4,3) NOT NULL DEFAULT 0.750 COMMENT '相似度阈值(0.000-1.000)',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_skill_kb (skill_id, knowledge_base_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='技能-知识库关联';

-- ============================
-- 知识库（Knowledge Base）
-- ============================

-- 知识库定义
-- 设计理由：知识库是文档的顶层容器，对应一个独立的知识域。
-- 知识库有两种用途：1) 常规知识库（纯文档管理/笔记）2) 向量化知识库（用于 RAG 检索）。
-- vectorized 标记是否已开启向量化，embedding_model_id 指定向量化使用的嵌入模型。
-- 未开启向量化的知识库仍可正常使用文档管理功能，向量化是可选增强。
-- chunk_size / chunk_overlap 是文档切片参数，不同知识库可根据文档特点差异化配置。
DROP TABLE IF EXISTS kb_knowledge_base;
CREATE TABLE kb_knowledge_base (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  estab_id BIGINT NOT NULL COMMENT '组织ID',
  kb_code VARCHAR(64) NOT NULL COMMENT '知识库编码',
  kb_name VARCHAR(128) NOT NULL COMMENT '知识库名称',
  description VARCHAR(512) DEFAULT NULL COMMENT '知识库描述',
  icon VARCHAR(64) DEFAULT NULL COMMENT '图标',
  visibility TINYINT NOT NULL DEFAULT 0 COMMENT '可见性 0私有 1组织内公开 2平台公开',
  vectorized TINYINT NOT NULL DEFAULT 0 COMMENT '是否开启向量化 1是 0否',
  embedding_model_id BIGINT DEFAULT NULL COMMENT '嵌入模型ID(开启向量化时必填)',
  chunk_size INT NOT NULL DEFAULT 512 COMMENT '文档切片大小(token数)',
  chunk_overlap INT NOT NULL DEFAULT 64 COMMENT '切片重叠大小(token数)',
  doc_count INT NOT NULL DEFAULT 0 COMMENT '文档数量(冗余计数)',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_kb_code (estab_id, kb_code),
  KEY idx_kb_visibility (estab_id, visibility, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库定义';

-- 知识库目录（树形结构）
-- 设计理由：知识库内的文档需要分类组织，目录树是最直观的方式。
-- parent_id=0 表示根目录下的一级目录。支持无限层级嵌套。
-- 目录本身不存储内容，仅作为文档的组织容器。
DROP TABLE IF EXISTS kb_folder;
CREATE TABLE kb_folder (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  knowledge_base_id BIGINT NOT NULL COMMENT '知识库ID',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父目录ID(0为根级)',
  folder_name VARCHAR(128) NOT NULL COMMENT '目录名称',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  KEY idx_folder_parent (knowledge_base_id, parent_id),
  KEY idx_folder_name (knowledge_base_id, folder_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库目录';

-- 知识库文档
-- 设计理由：文档是知识库的核心内容载体。
-- doc_type 支持多种格式：MD/PDF/DOCX/XLSX/TXT/HTML 等，覆盖主流文档类型。
-- 文档有两层内容：
-- 1) file_url 指向原始文件（存储在对象存储中）
-- 2) content 存储提取后的纯文本（用于全文检索和向量化切片）
-- char_count / token_count 用于统计和配额管控。
-- vector_status 跟踪向量化进度：未向量化的文档不参与 RAG 检索，
-- 但仍可作为常规文档浏览和管理。
DROP TABLE IF EXISTS kb_document;
CREATE TABLE kb_document (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  knowledge_base_id BIGINT NOT NULL COMMENT '知识库ID',
  folder_id BIGINT NOT NULL DEFAULT 0 COMMENT '目录ID(0为根目录)',
  doc_name VARCHAR(255) NOT NULL COMMENT '文档名称',
  doc_type VARCHAR(16) NOT NULL COMMENT '文档类型(MD/PDF/DOCX/XLSX/PPTX/TXT/HTML/CSV)',
  file_url VARCHAR(512) DEFAULT NULL COMMENT '原始文件存储地址',
  file_size BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
  content LONGTEXT DEFAULT NULL COMMENT '提取后的纯文本内容',
  char_count INT NOT NULL DEFAULT 0 COMMENT '字符数',
  token_count INT NOT NULL DEFAULT 0 COMMENT '估算token数',
  vector_status TINYINT NOT NULL DEFAULT 0 COMMENT '向量化状态 0未向量化 1向量化中 2已完成 3失败',
  vector_error VARCHAR(512) DEFAULT NULL COMMENT '向量化失败原因',
  chunk_count INT NOT NULL DEFAULT 0 COMMENT '切片数量',
  last_vectorized_at DATETIME(3) DEFAULT NULL COMMENT '最近向量化完成时间',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息(如文档元数据：作者、页数等)',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  KEY idx_doc_kb_folder (knowledge_base_id, folder_id, deleted),
  KEY idx_doc_type (knowledge_base_id, doc_type),
  KEY idx_doc_vector_status (knowledge_base_id, vector_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库文档';

-- 文档切片（Chunk）
-- 设计理由：RAG 的核心是将文档切片后向量化，检索时返回最相关的切片。
-- 此表存储切片的文本内容和在原文中的位置信息（chunk_index / start_offset / end_offset），
-- 便于前端高亮定位原文出处。
-- embedding_id 关联向量数据库中的向量记录（向量本身存储在 VectorStore 中，如 Milvus/PGVector，
-- 而非 MySQL，因为 MySQL 不适合高维向量检索）。
-- 此表是 MySQL 侧的元数据索引，与向量数据库形成互补。
DROP TABLE IF EXISTS kb_document_chunk;
CREATE TABLE kb_document_chunk (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  document_id BIGINT NOT NULL COMMENT '文档ID',
  knowledge_base_id BIGINT NOT NULL COMMENT '知识库ID(冗余，避免联表)',
  chunk_index INT NOT NULL COMMENT '切片序号(从0开始)',
  content TEXT NOT NULL COMMENT '切片文本内容',
  token_count INT NOT NULL DEFAULT 0 COMMENT '切片token数',
  start_offset INT DEFAULT NULL COMMENT '在原文中的起始字符偏移',
  end_offset INT DEFAULT NULL COMMENT '在原文中的结束字符偏移',
  embedding_id VARCHAR(64) DEFAULT NULL COMMENT '向量数据库中的向量ID(UUID)',
  metadata JSON DEFAULT NULL COMMENT '切片元数据(如标题层级、页码等)',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  UNIQUE KEY uk_doc_chunk (document_id, chunk_index),
  KEY idx_chunk_kb (knowledge_base_id, document_id),
  KEY idx_chunk_embedding (embedding_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文档切片';

-- ============================
-- AI 用量统计
-- ============================

-- AI 调用日志
-- 设计理由：每次 AI 模型调用都需要记录，用于：
-- 1) 用量统计与计费（input_tokens / output_tokens / total_cost）
-- 2) 质量监控（duration_ms / finish_reason）
-- 3) 问题排查（error_message）
-- 4) 审计合规（谁在什么时候用了什么模型做了什么）
-- conversation_id 可选，因为不是所有调用都来自对话（如批量摘要、后台任务等）。
-- 此表数据量大，建议按月分区或定期归档。
DROP TABLE IF EXISTS ai_usage_log;
CREATE TABLE ai_usage_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  estab_id BIGINT NOT NULL COMMENT '组织ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  conversation_id VARCHAR(36) DEFAULT NULL COMMENT '会话ID(可选)',
  model_id BIGINT NOT NULL COMMENT '模型ID',
  request_type VARCHAR(32) NOT NULL DEFAULT 'CHAT' COMMENT '请求类型(CHAT/EMBEDDING/IMAGE_GEN/TTS/STT/RERANK)',
  input_tokens INT NOT NULL DEFAULT 0 COMMENT '输入token数',
  output_tokens INT NOT NULL DEFAULT 0 COMMENT '输出token数',
  total_tokens INT NOT NULL DEFAULT 0 COMMENT '总token数',
  total_cost DECIMAL(12,6) DEFAULT NULL COMMENT '本次调用费用(美元)',
  duration_ms INT DEFAULT NULL COMMENT '耗时(毫秒)',
  finish_reason VARCHAR(32) DEFAULT NULL COMMENT '结束原因(stop/length/tool_calls/error)',
  success TINYINT NOT NULL DEFAULT 1 COMMENT '是否成功 1成功 0失败',
  error_message VARCHAR(512) DEFAULT NULL COMMENT '错误信息',
  ext_json JSON DEFAULT NULL COMMENT '扩展信息',
  create_by BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  update_by BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  delete_by BIGINT DEFAULT NULL COMMENT '删除人用户ID',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  lock_version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  gmt_create DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  gmt_modified DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  KEY idx_usage_estab_time (estab_id, gmt_create),
  KEY idx_usage_user_time (user_id, gmt_create),
  KEY idx_usage_model_time (model_id, gmt_create),
  KEY idx_usage_conv (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI调用日志';

SET FOREIGN_KEY_CHECKS = 1;
