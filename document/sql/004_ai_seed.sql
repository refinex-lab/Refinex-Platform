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
    ('openai',    'OpenAI',              'openai',     'https://api.openai.com',                                    NULL, 1,  10, '官方 OpenAI API',                            @super_user_id, @super_user_id, 0, 0),
    ('anthropic', 'Anthropic',           'anthropic',  'https://api.anthropic.com',                                 NULL, 1,  20, '官方 Anthropic API',                         @super_user_id, @super_user_id, 0, 0),
    ('deepseek',  'DeepSeek',            'openai',     'https://api.deepseek.com',                                  NULL, 1,  30, 'DeepSeek API（兼容 OpenAI 协议）',           @super_user_id, @super_user_id, 0, 0),
    ('zhipu',     '智谱AI',              'openai',     'https://open.bigmodel.cn/api/paas',                         NULL, 1,  40, '智谱 GLM 系列',                              @super_user_id, @super_user_id, 0, 0),
    ('minimax',   'MiniMax',             'openai',     'https://api.minimax.chat',                                  NULL, 1,  50, 'MiniMax API',                                @super_user_id, @super_user_id, 0, 0),
    ('google',    'Google Gemini',       'openai',     'https://generativelanguage.googleapis.com/v1beta/openai',   NULL, 1,  60, 'Google Gemini API（兼容 OpenAI 协议）',      @super_user_id, @super_user_id, 0, 0),
    ('xai',       'xAI Grok',            'openai',     'https://api.x.ai',                                          NULL, 1,  70, 'xAI Grok API（兼容 OpenAI 协议）',           @super_user_id, @super_user_id, 0, 0),
    ('doubao',    '豆包（火山引擎）',    'openai',     'https://ark.cn-beijing.volces.com/api/v3',                  NULL, 1,  80, '字节跳动豆包 / 火山方舟 API（兼容 OpenAI 协议）', @super_user_id, @super_user_id, 0, 0),
    ('qwen',      '通义千问',            'openai',     'https://dashscope.aliyuncs.com/compatible-mode/v1',         NULL, 1,  90, '阿里云通义千问 API（兼容 OpenAI 协议）',     @super_user_id, @super_user_id, 0, 0),
    ('moonshot',  'Kimi（月之暗面）',    'openai',     'https://api.moonshot.cn',                                   NULL, 1, 100, 'Moonshot Kimi API（兼容 OpenAI 协议）',      @super_user_id, @super_user_id, 0, 0),
    ('baidu',     '文心一言（百度千帆）','openai',     'https://qianfan.baidubce.com/v2',                           NULL, 1, 110, '百度千帆大模型平台 API（兼容 OpenAI 协议）', @super_user_id, @super_user_id, 0, 0),
    ('mistral',   'Mistral AI',          'openai',     'https://api.mistral.ai',                                    NULL, 1, 120, 'Mistral AI API（兼容 OpenAI 协议）',         @super_user_id, @super_user_id, 0, 0),
    ('groq',      'Groq',                'openai',     'https://api.groq.com/openai',                               NULL, 1, 130, 'Groq 高速推理平台 API（兼容 OpenAI 协议）',  @super_user_id, @super_user_id, 0, 0),
    ('cohere',    'Cohere',              'openai',     'https://api.cohere.com',                                    NULL, 1, 140, 'Cohere API（兼容 OpenAI 协议）',             @super_user_id, @super_user_id, 0, 0),
    ('together',  'Together AI',         'openai',     'https://api.together.xyz',                                  NULL, 1, 150, 'Together AI 开源模型托管平台（兼容 OpenAI 协议）', @super_user_id, @super_user_id, 0, 0),
    ('ollama',    'Ollama',              'ollama',     'http://localhost:11434',                                     NULL, 1, 160, 'Ollama 本地模型部署（自定义 base_url）',     @super_user_id, @super_user_id, 0, 0)
    ON DUPLICATE KEY UPDATE
                         provider_name = VALUES(provider_name), protocol = VALUES(protocol),
                         base_url      = VALUES(base_url),      status   = VALUES(status),
                         sort          = VALUES(sort),          remark   = VALUES(remark),
                         deleted = 0,  update_by = VALUES(update_by);

-- ----------------------------------------
-- AI 模型初始化（主流模型）
-- ----------------------------------------
SELECT id INTO @provider_openai    FROM ai_provider WHERE provider_code = 'openai'    AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_anthropic FROM ai_provider WHERE provider_code = 'anthropic' AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_deepseek  FROM ai_provider WHERE provider_code = 'deepseek'  AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_zhipu     FROM ai_provider WHERE provider_code = 'zhipu'     AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_minimax   FROM ai_provider WHERE provider_code = 'minimax'   AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_google    FROM ai_provider WHERE provider_code = 'google'    AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_xai       FROM ai_provider WHERE provider_code = 'xai'       AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_doubao    FROM ai_provider WHERE provider_code = 'doubao'    AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_qwen      FROM ai_provider WHERE provider_code = 'qwen'      AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_moonshot  FROM ai_provider WHERE provider_code = 'moonshot'  AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_baidu     FROM ai_provider WHERE provider_code = 'baidu'     AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_mistral   FROM ai_provider WHERE provider_code = 'mistral'   AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_groq      FROM ai_provider WHERE provider_code = 'groq'      AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_cohere    FROM ai_provider WHERE provider_code = 'cohere'    AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_together  FROM ai_provider WHERE provider_code = 'together'  AND deleted = 0 LIMIT 1;
SELECT id INTO @provider_ollama    FROM ai_provider WHERE provider_code = 'ollama'    AND deleted = 0 LIMIT 1;

INSERT INTO ai_model
(provider_id, model_code, model_name, model_type,
 cap_vision, cap_tool_call, cap_structured_output, cap_streaming,
 max_context_window, max_output_tokens, input_price, output_price,
 status, sort, remark, create_by, update_by, deleted, lock_version)
VALUES

    -- ======================================================
    -- OpenAI 聊天模型
    -- ======================================================
    (@provider_openai, 'gpt-4o',                  'GPT-4o',           1, 1, 1, 1, 1, 128000, 16384,  2.500000, 10.000000, 1,  10, NULL,                       @super_user_id, @super_user_id, 0, 0),
    (@provider_openai, 'gpt-4o-mini',             'GPT-4o Mini',      1, 1, 1, 1, 1, 128000, 16384,  0.150000,  0.600000, 1,  20, NULL,                       @super_user_id, @super_user_id, 0, 0),
    (@provider_openai, 'o3-mini',                 'o3 Mini',          1, 0, 1, 1, 1, 200000, 100000, 1.100000,  4.400000, 1,  30, '轻量级推理模型',           @super_user_id, @super_user_id, 0, 0),
    (@provider_openai, 'o4-mini',                 'o4 Mini',          1, 1, 1, 1, 1, 200000, 100000, 1.100000,  4.400000, 1,  40, '多模态轻量级推理模型',     @super_user_id, @super_user_id, 0, 0),
    -- OpenAI 嵌入模型
    (@provider_openai, 'text-embedding-3-small',  'Embedding 3 Small',2, 0, 0, 0, 0,   8191,   NULL,  0.020000,       NULL, 1, 100, '1536维嵌入模型',         @super_user_id, @super_user_id, 0, 0),
    (@provider_openai, 'text-embedding-3-large',  'Embedding 3 Large',2, 0, 0, 0, 0,   8191,   NULL,  0.130000,       NULL, 1, 110, '3072维嵌入模型',         @super_user_id, @super_user_id, 0, 0),
    -- OpenAI 图像生成模型
    (@provider_openai, 'dall-e-3',                'DALL-E 3',         3, 0, 0, 0, 0,   NULL,   NULL,      NULL,       NULL, 1, 200, '图像生成模型',           @super_user_id, @super_user_id, 0, 0),
    (@provider_openai, 'gpt-image-1',             'GPT Image 1',      3, 1, 0, 0, 0,   NULL,   NULL,      NULL,       NULL, 1, 210, '新一代多模态图像生成模型', @super_user_id, @super_user_id, 0, 0),
    -- OpenAI 语音模型
    (@provider_openai, 'whisper-1',               'Whisper',          4, 0, 0, 0, 0,   NULL,   NULL,      NULL,       NULL, 1, 300, '语音转文字',             @super_user_id, @super_user_id, 0, 0),
    (@provider_openai, 'tts-1',                   'TTS-1',            5, 0, 0, 0, 0,   4096,   NULL,      NULL,       NULL, 1, 400, '文字转语音（标准）',     @super_user_id, @super_user_id, 0, 0),
    (@provider_openai, 'gpt-4o-mini-tts',         'GPT-4o Mini TTS',  5, 0, 0, 0, 1,   4096,   NULL,      NULL,       NULL, 1, 410, '高质量TTS，支持指令控制语气', @super_user_id, @super_user_id, 0, 0),

    -- ======================================================
    -- Anthropic 聊天模型
    -- ======================================================
    (@provider_anthropic, 'claude-opus-4-20250514',   'Claude Opus 4',   1, 1, 1, 1, 1, 200000, 32000, 15.000000, 75.000000, 1,  10, '最强推理旗舰模型',       @super_user_id, @super_user_id, 0, 0),
    (@provider_anthropic, 'claude-sonnet-4-20250514', 'Claude Sonnet 4', 1, 1, 1, 1, 1, 200000, 16384,  3.000000, 15.000000, 1,  20, NULL,                     @super_user_id, @super_user_id, 0, 0),
    (@provider_anthropic, 'claude-haiku-4-20250514',  'Claude Haiku 4',  1, 1, 1, 1, 1, 200000, 16384,  0.800000,  4.000000, 1,  30, NULL,                     @super_user_id, @super_user_id, 0, 0),

    -- ======================================================
    -- DeepSeek 聊天模型
    -- ======================================================
    (@provider_deepseek, 'deepseek-chat',     'DeepSeek V3', 1, 0, 1, 1, 1, 65536,  8192, 0.270000, 1.100000, 1, 10, NULL,         @super_user_id, @super_user_id, 0, 0),
    (@provider_deepseek, 'deepseek-reasoner', 'DeepSeek R1', 1, 0, 0, 0, 1, 65536,  8192, 0.550000, 2.190000, 1, 20, '深度推理模型', @super_user_id, @super_user_id, 0, 0),

    -- ======================================================
    -- 智谱AI 聊天 / 嵌入 / 图像模型
    -- ======================================================
    (@provider_zhipu, 'glm-4-plus',    'GLM-4 Plus',   1, 1, 1, 1, 1, 128000, 4096, 0.500000, 0.500000, 1,  10, NULL,           @super_user_id, @super_user_id, 0, 0),
    (@provider_zhipu, 'glm-4-flash',   'GLM-4 Flash',  1, 0, 1, 1, 1, 128000, 4096, 0.000000, 0.000000, 1,  20, '免费快速模型', @super_user_id, @super_user_id, 0, 0),
    (@provider_zhipu, 'embedding-3',   'Embedding 3',  2, 0, 0, 0, 0,   8192, NULL, 0.005000,     NULL, 1, 100, '2048维嵌入模型', @super_user_id, @super_user_id, 0, 0),
    (@provider_zhipu, 'cogview-4',     'CogView 4',    3, 0, 0, 0, 0,   NULL, NULL,     NULL,     NULL, 1, 200, '智谱图像生成模型', @super_user_id, @super_user_id, 0, 0),

    -- ======================================================
    -- MiniMax 聊天 / 嵌入 / TTS 模型
    -- ======================================================
    (@provider_minimax, 'MiniMax-Text-01',    'MiniMax Text 01',   1, 0, 1, 1, 1, 1000000, 8192, 0.200000, 1.100000, 1,  10, '百万上下文窗口旗舰模型', @super_user_id, @super_user_id, 0, 0),
    (@provider_minimax, 'abab6.5s-chat',      'ABAB 6.5S',         1, 1, 1, 1, 1,  245760, 8192, 0.100000, 0.100000, 1,  20, '多模态高效对话模型',     @super_user_id, @super_user_id, 0, 0),
    (@provider_minimax, 'embo-01',            'Embo 01',           2, 0, 0, 0, 0,    4096, NULL, 0.010000,     NULL, 1, 100, '通用嵌入模型',           @super_user_id, @super_user_id, 0, 0),
    (@provider_minimax, 'speech-01-turbo',    'Speech 01 Turbo',   5, 0, 0, 0, 1,    NULL, NULL,     NULL,     NULL, 1, 200, '高速TTS模型',            @super_user_id, @super_user_id, 0, 0),

    -- ======================================================
    -- Google Gemini 聊天 / 嵌入 模型
    -- ======================================================
    (@provider_google, 'gemini-2.5-pro-preview-05-06',  'Gemini 2.5 Pro',        1, 1, 1, 1, 1, 1048576, 65536,  1.250000,  10.000000, 1,  10, '最强旗舰推理模型（>200K时价格翻倍）', @super_user_id, @super_user_id, 0, 0),
    (@provider_google, 'gemini-2.0-flash',               'Gemini 2.0 Flash',      1, 1, 1, 1, 1, 1048576,  8192,  0.100000,   0.400000, 1,  20, NULL,                                  @super_user_id, @super_user_id, 0, 0),
    (@provider_google, 'gemini-2.0-flash-lite',          'Gemini 2.0 Flash Lite', 1, 1, 1, 1, 1, 1048576,  8192,  0.075000,   0.300000, 1,  30, '最低成本多模态模型',                  @super_user_id, @super_user_id, 0, 0),
    (@provider_google, 'gemini-1.5-pro',                 'Gemini 1.5 Pro',        1, 1, 1, 1, 1, 2097152,  8192,  1.250000,   5.000000, 1,  40, '超长上下文（>128K时价格翻倍）',       @super_user_id, @super_user_id, 0, 0),
    (@provider_google, 'text-embedding-004',             'Text Embedding 004',    2, 0, 0, 0, 0,    2048,  NULL,  0.000000,       NULL, 1, 100, '768维嵌入模型',                        @super_user_id, @super_user_id, 0, 0),
    (@provider_google, 'imagen-3.0-generate-002',        'Imagen 3',              3, 0, 0, 0, 0,    NULL,  NULL,      NULL,       NULL, 1, 200, 'Google 旗舰图像生成模型',             @super_user_id, @super_user_id, 0, 0),

    -- ======================================================
    -- xAI Grok 聊天模型
    -- ======================================================
    (@provider_xai, 'grok-3',             'Grok 3',             1, 0, 1, 1, 1, 131072, 32768,  3.000000, 15.000000, 1, 10, '旗舰模型',                  @super_user_id, @super_user_id, 0, 0),
    (@provider_xai, 'grok-3-mini',        'Grok 3 Mini',        1, 0, 1, 1, 1, 131072, 32768,  0.300000,  0.500000, 1, 20, '轻量推理模型',              @super_user_id, @super_user_id, 0, 0),
    (@provider_xai, 'grok-2-vision-1212', 'Grok 2 Vision',      1, 1, 1, 1, 1,  32768,  8192,  2.000000, 10.000000, 1, 30, '多模态视觉理解模型',        @super_user_id, @super_user_id, 0, 0),

    -- ======================================================
    -- 豆包（火山引擎 ARK）聊天 / 嵌入模型
    -- 注意：model_code 填写火山方舟的 endpoint_id（形如 ep-xxx）或模型名称
    -- ======================================================
    (@provider_doubao, 'doubao-pro-256k',  'Doubao Pro 256K',  1, 0, 1, 1, 1, 256000, 12288, 0.350000, 0.950000, 1,  10, '超长上下文旗舰模型',     @super_user_id, @super_user_id, 0, 0),
    (@provider_doubao, 'doubao-pro-32k',   'Doubao Pro 32K',   1, 0, 1, 1, 1,  32768,  4096, 0.110000, 0.280000, 1,  20, '高性价比旗舰模型',       @super_user_id, @super_user_id, 0, 0),
    (@provider_doubao, 'doubao-lite-32k',  'Doubao Lite 32K',  1, 0, 0, 1, 1,  32768,  4096, 0.042000, 0.084000, 1,  30, '低成本快速模型',         @super_user_id, @super_user_id, 0, 0),
    (@provider_doubao, 'doubao-vision-pro-32k', 'Doubao Vision Pro 32K', 1, 1, 1, 1, 1, 32768, 4096, 0.140000, 0.420000, 1, 40, '多模态视觉理解模型', @super_user_id, @super_user_id, 0, 0),
    (@provider_doubao, 'doubao-embedding-large', 'Doubao Embedding Large', 2, 0, 0, 0, 0, 4096, NULL, 0.007000, NULL, 1, 100, '4096维嵌入模型', @super_user_id, @super_user_id, 0, 0),

    -- ======================================================
    -- 通义千问 聊天 / 嵌入模型
    -- ======================================================
    (@provider_qwen, 'qwen-plus-latest',      'Qwen Plus',          1, 0, 1, 1, 1, 131072,  8192, 0.400000, 1.200000, 1,  10, '综合性能旗舰模型',         @super_user_id, @super_user_id, 0, 0),
    (@provider_qwen, 'qwen-turbo-latest',     'Qwen Turbo',         1, 0, 1, 1, 1, 131072,  8192, 0.050000, 0.200000, 1,  20, '低延迟高效模型',           @super_user_id, @super_user_id, 0, 0),
    (@provider_qwen, 'qwen-max-latest',       'Qwen Max',           1, 0, 1, 1, 1,  32768,  8192, 1.600000, 6.400000, 1,  30, '旗舰推理模型',             @super_user_id, @super_user_id, 0, 0),
    (@provider_qwen, 'qvq-max',               'QVQ Max',            1, 1, 0, 0, 1,  32768,  8192, 2.700000,10.800000, 1,  40, '多模态视觉推理旗舰模型',   @super_user_id, @super_user_id, 0, 0),
    (@provider_qwen, 'text-embedding-v3',     'Text Embedding V3',  2, 0, 0, 0, 0,   8192,  NULL, 0.007000,     NULL, 1, 100, '1024/2048/4096维自适应嵌入', @super_user_id, @super_user_id, 0, 0),
    (@provider_qwen, 'qwen-vl-max-latest',    'Qwen VL Max',        1, 1, 1, 1, 1,  32768,  8192, 0.800000, 3.200000, 1,  50, '旗舰多模态视觉语言模型',   @super_user_id, @super_user_id, 0, 0),

    -- ======================================================
    -- Kimi（月之暗面）聊天 / 嵌入模型
    -- ======================================================
    (@provider_moonshot, 'moonshot-v1-8k',       'Kimi V1 8K',       1, 0, 1, 1, 1,   8192,  8192, 1.700000, 1.700000, 1,  10, NULL,                   @super_user_id, @super_user_id, 0, 0),
    (@provider_moonshot, 'moonshot-v1-32k',      'Kimi V1 32K',      1, 0, 1, 1, 1,  32768,  8192, 3.400000, 3.400000, 1,  20, NULL,                   @super_user_id, @super_user_id, 0, 0),
    (@provider_moonshot, 'moonshot-v1-128k',     'Kimi V1 128K',     1, 0, 1, 1, 1, 128000,  8192, 8.500000, 8.500000, 1,  30, '超长上下文模型',       @super_user_id, @super_user_id, 0, 0),
    (@provider_moonshot, 'kimi-latest',          'Kimi Latest',      1, 1, 1, 1, 1, 131072, 16384, 2.000000, 8.000000, 1,   5, '官方推荐最新旗舰版本', @super_user_id, @super_user_id, 0, 0),

    -- ======================================================
    -- 百度文心（千帆）聊天模型
    -- ======================================================
    (@provider_baidu, 'ernie-4.5-turbo-8k',     'ERNIE 4.5 Turbo 8K',  1, 1, 1, 1, 1,   8192,  2048, 0.140000,  0.420000, 1, 10, '旗舰多模态对话模型', @super_user_id, @super_user_id, 0, 0),
    (@provider_baidu, 'ernie-4.5-turbo-128k',   'ERNIE 4.5 Turbo 128K',1, 1, 1, 1, 1, 131072,  4096, 0.560000,  1.690000, 1, 20, '超长上下文旗舰模型', @super_user_id, @super_user_id, 0, 0),
    (@provider_baidu, 'ernie-lite-8k',          'ERNIE Lite 8K',        1, 0, 1, 1, 1,   8192,  2048, 0.000000,  0.000000, 1, 30, '免费轻量模型',       @super_user_id, @super_user_id, 0, 0),
    (@provider_baidu, 'ernie-speed-128k',       'ERNIE Speed 128K',     1, 0, 0, 1, 1, 131072,  4096, 0.000000,  0.000000, 1, 40, '免费超长上下文模型', @super_user_id, @super_user_id, 0, 0),

    -- ======================================================
    -- Mistral AI 聊天 / 嵌入模型
    -- ======================================================
    (@provider_mistral, 'mistral-large-latest',    'Mistral Large',    1, 0, 1, 1, 1, 131072, 8192,  2.000000,  6.000000, 1,  10, '旗舰推理模型',     @super_user_id, @super_user_id, 0, 0),
    (@provider_mistral, 'mistral-small-latest',    'Mistral Small',    1, 0, 1, 1, 1,  32768, 8192,  0.100000,  0.300000, 1,  20, '高性价比模型',     @super_user_id, @super_user_id, 0, 0),
    (@provider_mistral, 'codestral-latest',        'Codestral',        1, 0, 1, 1, 1, 256000, 8192,  0.300000,  0.900000, 1,  30, '专注代码生成模型', @super_user_id, @super_user_id, 0, 0),
    (@provider_mistral, 'pixtral-large-latest',    'Pixtral Large',    1, 1, 1, 1, 1, 131072, 8192,  2.000000,  6.000000, 1,  40, '多模态视觉旗舰',   @super_user_id, @super_user_id, 0, 0),
    (@provider_mistral, 'mistral-embed',           'Mistral Embed',    2, 0, 0, 0, 0,   8192, NULL,  0.100000,     NULL, 1, 100, '1024维嵌入模型',   @super_user_id, @super_user_id, 0, 0),

    -- ======================================================
    -- Groq（高速LPU推理平台）聊天模型
    -- ======================================================
    (@provider_groq, 'llama-3.3-70b-versatile',       'Llama 3.3 70B',         1, 0, 1, 1, 1, 131072,  8192, 0.590000, 0.790000, 1, 10, 'Meta 旗舰开源对话模型',     @super_user_id, @super_user_id, 0, 0),
    (@provider_groq, 'llama-3.1-8b-instant',           'Llama 3.1 8B Instant',  1, 0, 1, 1, 1, 131072,  8192, 0.050000, 0.080000, 1, 20, '极速轻量模型',               @super_user_id, @super_user_id, 0, 0),
    (@provider_groq, 'llama-3.2-90b-vision-preview',   'Llama 3.2 90B Vision',  1, 1, 0, 1, 1, 131072,  8192, 0.900000, 0.900000, 1, 30, '多模态视觉模型',             @super_user_id, @super_user_id, 0, 0),
    (@provider_groq, 'deepseek-r1-distill-llama-70b',  'DeepSeek R1 Distill 70B',1,0, 0, 0, 1, 131072,  8192, 0.750000, 0.990000, 1, 40, 'DeepSeek R1 蒸馏推理模型', @super_user_id, @super_user_id, 0, 0),
    (@provider_groq, 'gemma2-9b-it',                   'Gemma 2 9B',            1, 0, 1, 1, 1,   8192,  8192, 0.200000, 0.200000, 1, 50, 'Google 开源 Gemma 2 模型', @super_user_id, @super_user_id, 0, 0),
    (@provider_groq, 'whisper-large-v3-turbo',         'Whisper Large V3 Turbo',4, 0, 0, 0, 0,   NULL,  NULL,    NULL,    NULL,   1,100, '高速语音转文字',             @super_user_id, @super_user_id, 0, 0),

    -- ======================================================
    -- Cohere 聊天 / 嵌入模型
    -- ======================================================
    (@provider_cohere, 'command-r-plus-08-2024',          'Command R+',               1, 0, 1, 1, 1, 128000,  4096,  2.500000, 10.000000, 1,  10, '旗舰 RAG 和复杂任务模型', @super_user_id, @super_user_id, 0, 0),
    (@provider_cohere, 'command-r-08-2024',               'Command R',                1, 0, 1, 1, 1, 128000,  4096,  0.150000,  0.600000, 1,  20, '高效 RAG 对话模型',       @super_user_id, @super_user_id, 0, 0),
    (@provider_cohere, 'command-a-03-2025',               'Command A',                1, 0, 1, 1, 1, 256000,  8192,  2.500000, 10.000000, 1,   5, '最新旗舰企业级模型',     @super_user_id, @super_user_id, 0, 0),
    (@provider_cohere, 'embed-multilingual-v3.0',         'Embed Multilingual V3',    2, 0, 0, 0, 0,    512,  NULL,  0.100000,     NULL, 1, 100, '1024维多语言嵌入模型',   @super_user_id, @super_user_id, 0, 0),
    (@provider_cohere, 'embed-multilingual-light-v3.0',   'Embed Multilingual Light', 2, 0, 0, 0, 0,    512,  NULL,  0.100000,     NULL, 1, 110, '384维轻量多语言嵌入',    @super_user_id, @super_user_id, 0, 0),

    -- ======================================================
    -- Together AI（开源模型托管）聊天 / 嵌入模型
    -- ======================================================
    (@provider_together, 'meta-llama/Llama-3.3-70B-Instruct-Turbo',       'Llama 3.3 70B Turbo',   1, 0, 1, 1, 1, 131072,  8192, 0.880000, 0.880000, 1,  10, 'Meta 旗舰开源模型',        @super_user_id, @super_user_id, 0, 0),
    (@provider_together, 'meta-llama/Llama-3.2-11B-Vision-Instruct-Turbo','Llama 3.2 11B Vision',  1, 1, 0, 1, 1,  32768,  4096, 0.180000, 0.180000, 1,  20, '轻量多模态视觉模型',      @super_user_id, @super_user_id, 0, 0),
    (@provider_together, 'deepseek-ai/DeepSeek-R1',                        'DeepSeek R1',           1, 0, 0, 0, 1,  32768,  8192, 3.000000, 7.000000, 1,  30, 'DeepSeek 深度推理旗舰',   @super_user_id, @super_user_id, 0, 0),
    (@provider_together, 'mistralai/Mixtral-8x22B-Instruct-v0.1',          'Mixtral 8x22B',         1, 0, 1, 1, 1,  65536,  8192, 1.200000, 1.200000, 1,  40, 'Mistral MoE 大规模模型',  @super_user_id, @super_user_id, 0, 0),
    (@provider_together, 'togethercomputer/m2-bert-80M-8k-retrieval',      'M2 BERT 8K Retrieval',  2, 0, 0, 0, 0,   8192,  NULL, 0.008000,     NULL, 1, 100, '768维检索嵌入模型',       @super_user_id, @super_user_id, 0, 0),

    -- ======================================================
    -- Ollama 本地模型（base_url 按实际部署地址修改）
    -- ======================================================
    (@provider_ollama, 'llama3.2',      'Llama 3.2 3B',  1, 0, 0, 0, 1,  32768,  4096, 0.000000, 0.000000, 1, 10, '本地部署 Meta Llama 3.2 3B',  @super_user_id, @super_user_id, 0, 0),
    (@provider_ollama, 'llama3.3',      'Llama 3.3 70B', 1, 0, 1, 1, 1, 131072,  8192, 0.000000, 0.000000, 1, 20, '本地部署 Meta Llama 3.3 70B', @super_user_id, @super_user_id, 0, 0),
    (@provider_ollama, 'qwen2.5',       'Qwen 2.5 7B',   1, 0, 1, 1, 1,  32768,  4096, 0.000000, 0.000000, 1, 30, '本地部署通义千问 2.5 7B',     @super_user_id, @super_user_id, 0, 0),
    (@provider_ollama, 'deepseek-r1',   'DeepSeek R1',   1, 0, 0, 0, 1,  32768,  8192, 0.000000, 0.000000, 1, 40, '本地部署 DeepSeek R1 蒸馏版', @super_user_id, @super_user_id, 0, 0),
    (@provider_ollama, 'nomic-embed-text', 'Nomic Embed Text', 2, 0, 0, 0, 0, 8192, NULL, 0.000000, NULL, 1, 100, '本地嵌入模型，768维',        @super_user_id, @super_user_id, 0, 0)

    ON DUPLICATE KEY UPDATE
                         model_name             = VALUES(model_name),
                         model_type             = VALUES(model_type),
                         cap_vision             = VALUES(cap_vision),
                         cap_tool_call          = VALUES(cap_tool_call),
                         cap_structured_output  = VALUES(cap_structured_output),
                         cap_streaming          = VALUES(cap_streaming),
                         max_context_window     = VALUES(max_context_window),
                         max_output_tokens      = VALUES(max_output_tokens),
                         input_price            = VALUES(input_price),
                         output_price           = VALUES(output_price),
                         status                 = VALUES(status),
                         sort                   = VALUES(sort),
                         remark                 = VALUES(remark),
                         deleted                = 0,
                         update_by              = VALUES(update_by);

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
