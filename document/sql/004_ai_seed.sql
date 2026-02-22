-- ============================================================================
-- Refinex Platform AI 与知识库模块初始化数据（幂等）
-- 说明：
-- 1) 本脚本依赖 003_ai_schema.sql 已执行完成
-- 2) 使用 UPSERT（ON DUPLICATE KEY UPDATE）支持重复执行
-- 3) 同时补充 001 体系中 AI 相关的值集、菜单、角色权限数据
-- ============================================================================

USE refinex_platform;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- ----------------------------------------
-- 常量
-- ----------------------------------------
SET @seed_operator_id := 0;
SET @seed_now := NOW(3);

SELECT id INTO @super_user_id FROM def_user WHERE user_code = 'U_REFINEX_SUPER_ADMIN' AND deleted = 0 LIMIT 1;
SELECT id INTO @estab_id FROM def_estab WHERE estab_code = 'ESTAB_REFINEX' AND deleted = 0 LIMIT 1;
SELECT id INTO @role_platform_super_id FROM scr_role WHERE estab_id = 0 AND role_code = 'PLATFORM_SUPER_ADMIN' AND deleted = 0 LIMIT 1;
SELECT id INTO @system_tenant_id FROM scr_system WHERE system_code = 'tenant' AND deleted = 0 LIMIT 1;

-- ----------------------------------------
-- AI 相关值集补充
-- ----------------------------------------
INSERT INTO app_valueset
(set_code, set_name, status, sort, description, create_by, update_by, deleted, lock_version)
VALUES
  ('ai_model_type', 'AI模型类型', 1, 200, 'AI模型能力类型', @seed_operator_id, @seed_operator_id, 0, 0),
  ('ai_doc_type', '知识库文档类型', 1, 210, '知识库支持的文档格式', @seed_operator_id, @seed_operator_id, 0, 0),
  ('ai_vector_status', '向量化状态', 1, 220, '文档向量化处理状态', @seed_operator_id, @seed_operator_id, 0, 0)
ON DUPLICATE KEY UPDATE
  set_name = VALUES(set_name), status = VALUES(status), sort = VALUES(sort),
  description = VALUES(description), deleted = 0, update_by = VALUES(update_by);

INSERT INTO app_value
(set_code, value_code, value_name, value_desc, status, is_default, sort, create_by, update_by, deleted, lock_version)
VALUES
  ('ai_model_type', '1', '聊天', '对话/文本生成模型', 1, 1, 10, @seed_operator_id, @seed_operator_id, 0, 0),
  ('ai_model_type', '2', '嵌入', '文本向量化模型', 1, 0, 20, @seed_operator_id, @seed_operator_id, 0, 0),
  ('ai_model_type', '3', '图像生成', '文生图模型', 1, 0, 30, @seed_operator_id, @seed_operator_id, 0, 0),
  ('ai_model_type', '4', '语音转文字', 'STT/ASR模型', 1, 0, 40, @seed_operator_id, @seed_operator_id, 0, 0),
  ('ai_model_type', '5', '文字转语音', 'TTS模型', 1, 0, 50, @seed_operator_id, @seed_operator_id, 0, 0),
  ('ai_model_type', '6', '重排序', 'Reranker模型', 1, 0, 60, @seed_operator_id, @seed_operator_id, 0, 0),

  ('ai_doc_type', 'MD', 'Markdown', NULL, 1, 1, 10, @seed_operator_id, @seed_operator_id, 0, 0),
  ('ai_doc_type', 'PDF', 'PDF', NULL, 1, 0, 20, @seed_operator_id, @seed_operator_id, 0, 0),
  ('ai_doc_type', 'DOCX', 'Word文档', NULL, 1, 0, 30, @seed_operator_id, @seed_operator_id, 0, 0),
  ('ai_doc_type', 'XLSX', 'Excel表格', NULL, 1, 0, 40, @seed_operator_id, @seed_operator_id, 0, 0),
  ('ai_doc_type', 'PPTX', 'PPT演示', NULL, 1, 0, 50, @seed_operator_id, @seed_operator_id, 0, 0),
  ('ai_doc_type', 'TXT', '纯文本', NULL, 1, 0, 60, @seed_operator_id, @seed_operator_id, 0, 0),
  ('ai_doc_type', 'HTML', 'HTML网页', NULL, 1, 0, 70, @seed_operator_id, @seed_operator_id, 0, 0),
  ('ai_doc_type', 'CSV', 'CSV表格', NULL, 1, 0, 80, @seed_operator_id, @seed_operator_id, 0, 0),

  ('ai_vector_status', '0', '未向量化', NULL, 1, 1, 10, @seed_operator_id, @seed_operator_id, 0, 0),
  ('ai_vector_status', '1', '向量化中', NULL, 1, 0, 20, @seed_operator_id, @seed_operator_id, 0, 0),
  ('ai_vector_status', '2', '已完成', NULL, 1, 0, 30, @seed_operator_id, @seed_operator_id, 0, 0),
  ('ai_vector_status', '3', '失败', NULL, 1, 0, 40, @seed_operator_id, @seed_operator_id, 0, 0)
ON DUPLICATE KEY UPDATE
  value_name = VALUES(value_name), value_desc = VALUES(value_desc),
  status = VALUES(status), is_default = VALUES(is_default), sort = VALUES(sort),
  deleted = 0, update_by = VALUES(update_by);

-- ----------------------------------------
-- AI 供应商初始化
-- ----------------------------------------
INSERT INTO ai_provider
(provider_code, provider_name, protocol, base_url, icon_url, status, sort, remark, create_by, update_by, deleted, lock_version)
VALUES
  ('openai', 'OpenAI', 'openai', 'https://api.openai.com', NULL, 1, 10, '官方 OpenAI API', @super_user_id, @super_user_id, 0, 0),
  ('anthropic', 'Anthropic', 'anthropic', 'https://api.anthropic.com', NULL, 1, 20, '官方 Anthropic API', @super_user_id, @super_user_id, 0, 0),
  ('deepseek', 'DeepSeek', 'openai', 'https://api.deepseek.com', NULL, 1, 30, 'DeepSeek API（兼容 OpenAI 协议）', @super_user_id, @super_user_id, 0, 0),
  ('zhipu', '智谱AI', 'openai', 'https://open.bigmodel.cn/api/paas', NULL, 1, 40, '智谱 GLM 系列', @super_user_id, @super_user_id, 0, 0),
  ('minimax', 'MiniMax', 'openai', 'https://api.minimax.chat', NULL, 1, 50, 'MiniMax API', @super_user_id, @super_user_id, 0, 0)
ON DUPLICATE KEY UPDATE
  provider_name = VALUES(provider_name), protocol = VALUES(protocol),
  base_url = VALUES(base_url), status = VALUES(status), sort = VALUES(sort),
  remark = VALUES(remark), deleted = 0, update_by = VALUES(update_by);

-- ----------------------------------------
-- AI 模型初始化（主流模型）
-- ----------------------------------------
SELECT id INTO @provider_openai FROM ai_provider WHERE provider_code = 'openai' AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_anthropic FROM ai_provider WHERE provider_code = 'anthropic' AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_deepseek FROM ai_provider WHERE provider_code = 'deepseek' AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_zhipu FROM ai_provider WHERE provider_code = 'zhipu' AND deleted = 0 LIMIT 1;

INSERT INTO ai_model
(provider_id, model_code, model_name, model_type,
 cap_vision, cap_tool_call, cap_structured_output, cap_streaming,
 max_context_window, max_output_tokens, input_price, output_price,
 status, sort, remark, create_by, update_by, deleted, lock_version)
VALUES
  -- OpenAI 聊天模型
  (@provider_openai, 'gpt-4o', 'GPT-4o', 1,
   1, 1, 1, 1, 128000, 16384, 2.500000, 10.000000,
   1, 10, NULL, @super_user_id, @super_user_id, 0, 0),
  (@provider_openai, 'gpt-4o-mini', 'GPT-4o Mini', 1,
   1, 1, 1, 1, 128000, 16384, 0.150000, 0.600000,
   1, 20, NULL, @super_user_id, @super_user_id, 0, 0),
  -- OpenAI 嵌入模型
  (@provider_openai, 'text-embedding-3-small', 'Embedding 3 Small', 2,
   0, 0, 0, 0, 8191, NULL, 0.020000, NULL,
   1, 100, '1536维嵌入模型', @super_user_id, @super_user_id, 0, 0),
  (@provider_openai, 'text-embedding-3-large', 'Embedding 3 Large', 2,
   0, 0, 0, 0, 8191, NULL, 0.130000, NULL,
   1, 110, '3072维嵌入模型', @super_user_id, @super_user_id, 0, 0),
  -- OpenAI 语音模型
  (@provider_openai, 'whisper-1', 'Whisper', 4,
   0, 0, 0, 0, NULL, NULL, NULL, NULL,
   1, 200, '语音转文字', @super_user_id, @super_user_id, 0, 0),
  (@provider_openai, 'tts-1', 'TTS-1', 5,
   0, 0, 0, 0, 4096, NULL, NULL, NULL,
   1, 210, '文字转语音', @super_user_id, @super_user_id, 0, 0),
  (@provider_openai, 'gpt-4o-mini-tts', 'GPT-4o Mini TTS', 5,
   0, 0, 0, 1, 4096, NULL, NULL, NULL,
   1, 220, '高质量TTS，支持指令控制语气', @super_user_id, @super_user_id, 0, 0),

  -- Anthropic 聊天模型
  (@provider_anthropic, 'claude-sonnet-4-20250514', 'Claude Sonnet 4', 1,
   1, 1, 1, 1, 200000, 16384, 3.000000, 15.000000,
   1, 10, NULL, @super_user_id, @super_user_id, 0, 0),
  (@provider_anthropic, 'claude-haiku-4-20250514', 'Claude Haiku 4', 1,
   1, 1, 1, 1, 200000, 16384, 0.800000, 4.000000,
   1, 20, NULL, @super_user_id, @super_user_id, 0, 0),

  -- DeepSeek 聊天模型
  (@provider_deepseek, 'deepseek-chat', 'DeepSeek V3', 1,
   0, 1, 1, 1, 65536, 8192, 0.270000, 1.100000,
   1, 10, NULL, @super_user_id, @super_user_id, 0, 0),
  (@provider_deepseek, 'deepseek-reasoner', 'DeepSeek R1', 1,
   0, 0, 0, 1, 65536, 8192, 0.550000, 2.190000,
   1, 20, '深度推理模型', @super_user_id, @super_user_id, 0, 0),

  -- 智谱 聊天模型
  (@provider_zhipu, 'glm-4-plus', 'GLM-4 Plus', 1,
   1, 1, 1, 1, 128000, 4096, 0.500000, 0.500000,
   1, 10, NULL, @super_user_id, @super_user_id, 0, 0),
  -- 智谱 嵌入模型
  (@provider_zhipu, 'embedding-3', 'Embedding 3', 2,
   0, 0, 0, 0, 8192, NULL, 0.005000, NULL,
   1, 100, '2048维嵌入模型', @super_user_id, @super_user_id, 0, 0),

  -- OpenAI 图像生成模型
  (@provider_openai, 'dall-e-3', 'DALL-E 3', 3,
   0, 0, 0, 0, NULL, NULL, NULL, NULL,
   1, 300, '图像生成模型', @super_user_id, @super_user_id, 0, 0),
  -- 智谱 图像生成模型
  (@provider_zhipu, 'cogview-4', 'CogView 4', 3,
   0, 0, 0, 0, NULL, NULL, NULL, NULL,
   1, 310, '智谱图像生成模型', @super_user_id, @super_user_id, 0, 0)
ON DUPLICATE KEY UPDATE
  model_name = VALUES(model_name), model_type = VALUES(model_type),
  cap_vision = VALUES(cap_vision), cap_tool_call = VALUES(cap_tool_call),
  cap_structured_output = VALUES(cap_structured_output), cap_streaming = VALUES(cap_streaming),
  max_context_window = VALUES(max_context_window), max_output_tokens = VALUES(max_output_tokens),
  input_price = VALUES(input_price), output_price = VALUES(output_price),
  status = VALUES(status), sort = VALUES(sort), remark = VALUES(remark),
  deleted = 0, update_by = VALUES(update_by);

-- ----------------------------------------
-- 租户菜单初始化（AI 与知识库）
-- ----------------------------------------
INSERT INTO scr_menu
(estab_id, system_id, parent_id, menu_code, menu_name, menu_type, path,
 icon, is_builtin, visible, is_frame, status, sort,
 create_by, update_by, deleted, lock_version)
VALUES
  (@estab_id, @system_tenant_id, 0, 'AI_MENU_ROOT', 'AI 助手', 0, '/ai', 'bot', 1, 1, 0, 1, 20, @super_user_id, @super_user_id, 0, 0),
  (@estab_id, @system_tenant_id, 0, 'AI_CHAT', 'AI 对话', 1, '/ai/chat', 'message-square', 1, 1, 0, 1, 21, @super_user_id, @super_user_id, 0, 0),
  (@estab_id, @system_tenant_id, 0, 'AI_MODEL_MGMT', '模型管理', 1, '/ai/models', 'cpu', 1, 1, 0, 1, 22, @super_user_id, @super_user_id, 0, 0),
  (@estab_id, @system_tenant_id, 0, 'AI_PROMPT_MGMT', 'Prompt 管理', 1, '/ai/prompts', 'file-text', 1, 1, 0, 1, 23, @super_user_id, @super_user_id, 0, 0),
  (@estab_id, @system_tenant_id, 0, 'AI_SKILL_MGMT', '技能管理', 1, '/ai/skills', 'zap', 1, 1, 0, 1, 24, @super_user_id, @super_user_id, 0, 0),
  (@estab_id, @system_tenant_id, 0, 'AI_TOOL_MGMT', '工具管理', 1, '/ai/tools', 'wrench', 1, 1, 0, 1, 25, @super_user_id, @super_user_id, 0, 0),
  (@estab_id, @system_tenant_id, 0, 'KB_MENU_ROOT', '知识库', 0, '/kb', 'library', 1, 1, 0, 1, 30, @super_user_id, @super_user_id, 0, 0),
  (@estab_id, @system_tenant_id, 0, 'KB_LIST', '我的知识库', 1, '/kb/list', 'book-open', 1, 1, 0, 1, 31, @super_user_id, @super_user_id, 0, 0)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id), menu_name = VALUES(menu_name),
  menu_type = VALUES(menu_type), path = VALUES(path), icon = VALUES(icon),
  is_builtin = VALUES(is_builtin), visible = VALUES(visible),
  is_frame = VALUES(is_frame), status = VALUES(status), sort = VALUES(sort),
  deleted = 0, update_by = VALUES(update_by);

-- 设置 AI 子菜单的 parent_id
SELECT id INTO @menu_ai_root FROM scr_menu WHERE estab_id = @estab_id AND system_id = @system_tenant_id AND menu_code = 'AI_MENU_ROOT' AND deleted = 0 LIMIT 1;
SELECT id INTO @menu_kb_root FROM scr_menu WHERE estab_id = @estab_id AND system_id = @system_tenant_id AND menu_code = 'KB_MENU_ROOT' AND deleted = 0 LIMIT 1;

UPDATE scr_menu SET parent_id = @menu_ai_root WHERE estab_id = @estab_id AND menu_code IN ('AI_CHAT', 'AI_MODEL_MGMT', 'AI_PROMPT_MGMT', 'AI_SKILL_MGMT', 'AI_TOOL_MGMT') AND deleted = 0;
UPDATE scr_menu SET parent_id = @menu_kb_root WHERE estab_id = @estab_id AND menu_code IN ('KB_LIST') AND deleted = 0;

-- 为 AI/KB 菜单添加操作定义
SELECT id INTO @menu_ai_chat FROM scr_menu WHERE estab_id = @estab_id AND menu_code = 'AI_CHAT' AND deleted = 0 LIMIT 1;
SELECT id INTO @menu_ai_model FROM scr_menu WHERE estab_id = @estab_id AND menu_code = 'AI_MODEL_MGMT' AND deleted = 0 LIMIT 1;
SELECT id INTO @menu_ai_prompt FROM scr_menu WHERE estab_id = @estab_id AND menu_code = 'AI_PROMPT_MGMT' AND deleted = 0 LIMIT 1;
SELECT id INTO @menu_ai_skill FROM scr_menu WHERE estab_id = @estab_id AND menu_code = 'AI_SKILL_MGMT' AND deleted = 0 LIMIT 1;
SELECT id INTO @menu_ai_tool FROM scr_menu WHERE estab_id = @estab_id AND menu_code = 'AI_TOOL_MGMT' AND deleted = 0 LIMIT 1;
SELECT id INTO @menu_kb_list FROM scr_menu WHERE estab_id = @estab_id AND menu_code = 'KB_LIST' AND deleted = 0 LIMIT 1;

INSERT INTO scr_menu_op
(menu_id, op_code, op_name, status, sort, ext_json, create_by, update_by, deleted, lock_version)
VALUES
  (@menu_ai_chat, 'VIEW', '查看', 1, 10, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_ai_model, 'VIEW', '查看', 1, 10, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_ai_model, 'ADD', '新增', 1, 20, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_ai_model, 'EDIT', '编辑', 1, 30, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_ai_model, 'DELETE', '删除', 1, 40, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_ai_prompt, 'VIEW', '查看', 1, 10, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_ai_prompt, 'ADD', '新增', 1, 20, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_ai_prompt, 'EDIT', '编辑', 1, 30, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_ai_prompt, 'DELETE', '删除', 1, 40, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_ai_skill, 'VIEW', '查看', 1, 10, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_ai_skill, 'ADD', '新增', 1, 20, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_ai_skill, 'EDIT', '编辑', 1, 30, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_ai_skill, 'DELETE', '删除', 1, 40, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_ai_tool, 'VIEW', '查看', 1, 10, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_ai_tool, 'ADD', '新增', 1, 20, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_ai_tool, 'EDIT', '编辑', 1, 30, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_ai_tool, 'DELETE', '删除', 1, 40, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_kb_list, 'VIEW', '查看', 1, 10, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_kb_list, 'ADD', '新增', 1, 20, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_kb_list, 'EDIT', '编辑', 1, 30, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0),
  (@menu_kb_list, 'DELETE', '删除', 1, 40, JSON_OBJECT('seed', '004_ai_seed'), @super_user_id, @super_user_id, 0, 0)
ON DUPLICATE KEY UPDATE
  op_name = VALUES(op_name), status = VALUES(status), sort = VALUES(sort),
  ext_json = VALUES(ext_json), deleted = 0, update_by = VALUES(update_by);

-- 平台超管 -> AI/KB 全菜单授权
INSERT INTO scr_role_menu
(role_id, menu_id, granted_by, granted_time, create_by, update_by, deleted, lock_version)
SELECT @role_platform_super_id, m.id, @super_user_id, @seed_now, @super_user_id, @super_user_id, 0, 0
FROM scr_menu m
WHERE m.deleted = 0
  AND m.menu_code IN ('AI_MENU_ROOT','AI_CHAT','AI_MODEL_MGMT','AI_PROMPT_MGMT','AI_SKILL_MGMT','AI_TOOL_MGMT','KB_MENU_ROOT','KB_LIST')
ON DUPLICATE KEY UPDATE
  granted_by = VALUES(granted_by), granted_time = VALUES(granted_time),
  deleted = 0, update_by = VALUES(update_by);

-- 平台超管 -> AI/KB 全菜单操作授权
INSERT INTO scr_role_menu_op
(role_id, menu_op_id, granted_by, granted_time, create_by, update_by, deleted, lock_version)
SELECT @role_platform_super_id, mo.id, @super_user_id, @seed_now, @super_user_id, @super_user_id, 0, 0
FROM scr_menu_op mo
JOIN scr_menu m ON m.id = mo.menu_id
WHERE m.deleted = 0 AND mo.deleted = 0
  AND m.menu_code IN ('AI_CHAT','AI_MODEL_MGMT','AI_PROMPT_MGMT','AI_SKILL_MGMT','AI_TOOL_MGMT','KB_LIST')
ON DUPLICATE KEY UPDATE
  granted_by = VALUES(granted_by), granted_time = VALUES(granted_time),
  deleted = 0, update_by = VALUES(update_by);

-- ----------------------------------------
-- 内置 Prompt 模板
-- ----------------------------------------
INSERT INTO ai_prompt_template
(estab_id, prompt_code, prompt_name, category, content, variables, language,
 is_builtin, status, sort, remark, create_by, update_by, deleted, lock_version)
VALUES
(0, 'SYSTEM_DEFAULT_CHAT', '默认对话', 'chat',
 '你是 Refinex AI 助手，一个专业、友好的人工智能。请用简洁准确的语言回答用户的问题。',
 NULL, 'zh-CN', 1, 1, 10, '系统默认对话 Prompt',
 @super_user_id, @super_user_id, 0, 0),
(0, 'SYSTEM_RAG_QA', 'RAG 问答', 'rag',
 '请根据以下参考资料回答用户的问题。如果参考资料中没有相关信息，请如实告知。\n\n参考资料：\n{{context}}\n\n用户问题：{{question}}',
 JSON_ARRAY(
   JSON_OBJECT('name', 'context', 'desc', '检索到的参考文档内容', 'required', true),
   JSON_OBJECT('name', 'question', 'desc', '用户提出的问题', 'required', true)
 ),
 'zh-CN', 1, 1, 20, '基于知识库检索的问答 Prompt',
 @super_user_id, @super_user_id, 0, 0),
(0, 'SYSTEM_SUMMARY', '文档摘要', 'summary',
 '请对以下内容进行摘要，提取核心要点，用简洁的条目列出：\n\n{{content}}',
 JSON_ARRAY(
   JSON_OBJECT('name', 'content', 'desc', '待摘要的文本内容', 'required', true)
 ),
 'zh-CN', 1, 1, 30, '文档摘要 Prompt',
 @super_user_id, @super_user_id, 0, 0),
(0, 'SYSTEM_TRANSLATE', '翻译', 'translate',
 '请将以下内容翻译为{{target_language}}，保持原文格式和语义：\n\n{{content}}',
 JSON_ARRAY(
   JSON_OBJECT('name', 'content', 'desc', '待翻译的文本', 'required', true),
   JSON_OBJECT('name', 'target_language', 'desc', '目标语言', 'required', true)
 ),
 'zh-CN', 1, 1, 40, '翻译 Prompt',
 @super_user_id, @super_user_id, 0, 0)
ON DUPLICATE KEY UPDATE
  prompt_name = VALUES(prompt_name), category = VALUES(category),
  content = VALUES(content), variables = VALUES(variables),
  is_builtin = VALUES(is_builtin), status = VALUES(status), sort = VALUES(sort),
  deleted = 0, update_by = VALUES(update_by);

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
