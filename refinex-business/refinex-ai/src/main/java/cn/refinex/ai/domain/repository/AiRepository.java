package cn.refinex.ai.domain.repository;

import cn.refinex.ai.domain.model.entity.*;
import cn.refinex.base.response.PageResponse;

import java.util.Collection;
import java.util.List;

/**
 * AI 模块仓储接口
 *
 * @author refinex
 */
public interface AiRepository {

    // ── Provider ──

    /**
     * 查询供应商列表
     *
     * @param status      状态 1启用 0停用
     * @param keyword     搜索关键词
     * @param currentPage 当前页码
     * @param pageSize    每页数量
     * @return 供应商分页列表
     */
    PageResponse<ProviderEntity> listProviders(Integer status, String keyword, int currentPage, int pageSize);

    /**
     * 查询全部供应商
     *
     * @param status 状态 1启用 0停用
     * @return 供应商列表
     */
    List<ProviderEntity> listAllProviders(Integer status);

    /**
     * 查询供应商
     *
     * @param providerId 供应商ID
     * @return 供应商
     */
    ProviderEntity findProviderById(Long providerId);

    /**
     * 批量查询供应商
     *
     * @param providerIds 供应商ID集合
     * @return 供应商列表
     */
    List<ProviderEntity> findProvidersByIds(Collection<Long> providerIds);

    /**
     * 统计供应商编码数量
     *
     * @param providerCode      供应商编码
     * @param excludeProviderId 排除的供应商ID
     * @return 供应商编码数量
     */
    long countProviderCode(String providerCode, Long excludeProviderId);

    /**
     * 插入供应商
     *
     * @param provider 供应商
     * @return 插入的供应商
     */
    ProviderEntity insertProvider(ProviderEntity provider);

    /**
     * 更新供应商
     *
     * @param provider 供应商
     */
    void updateProvider(ProviderEntity provider);

    /**
     * 删除供应商
     *
     * @param providerId 供应商ID
     */
    void deleteProviderById(Long providerId);

    // ── Model ──

    /**
     * 查询模型分页列表
     *
     * @param providerId  供应商ID
     * @param modelType   模型类型 1聊天 2嵌入 3图像生成 4语音转文字 5文字转语音 6重排序
     * @param status      状态 1启用 0停用
     * @param keyword     搜索关键词
     * @param currentPage 当前页码
     * @param pageSize    每页数量
     * @return 模型分页列表
     */
    PageResponse<ModelEntity> listModels(Long providerId, Integer modelType, Integer status, String keyword, int currentPage, int pageSize);

    /**
     * 查询供应商下的模型列表
     *
     * @param providerId 供应商ID
     * @return 模型列表
     */
    List<ModelEntity> listModelsByProviderId(Long providerId);

    /**
     * 查询模型
     *
     * @param modelId 模型ID
     * @return 模型
     */
    ModelEntity findModelById(Long modelId);

    /**
     * 批量查询模型
     *
     * @param modelIds 模型ID集合
     * @return 模型列表
     */
    List<ModelEntity> findModelsByIds(Collection<Long> modelIds);

    /**
     * 统计模型编码数量
     *
     * @param providerId     供应商ID
     * @param modelCode      模型编码
     * @param excludeModelId 排除的模型ID
     * @return 模型编码数量
     */
    long countModelCode(Long providerId, String modelCode, Long excludeModelId);

    /**
     * 插入模型
     *
     * @param model 模型
     * @return 插入的模型
     */
    ModelEntity insertModel(ModelEntity model);

    /**
     * 更新模型
     *
     * @param model 模型
     */
    void updateModel(ModelEntity model);

    /**
     * 删除模型
     *
     * @param modelId 模型ID
     */
    void deleteModelById(Long modelId);

    /**
     * 统计供应商下的模型数量
     *
     * @param providerId 供应商ID
     * @return 模型数量
     */
    long countModelsByProviderId(Long providerId);

    // ── PromptTemplate ──

    /**
     * 查询Prompt模板分页列表
     *
     * @param estabId     组织ID
     * @param status      状态 1启用 0停用
     * @param category    分类
     * @param keyword     搜索关键词
     * @param currentPage 当前页码
     * @param pageSize    每页数量
     * @return Prompt模板分页列表
     */
    PageResponse<PromptTemplateEntity> listPromptTemplates(Long estabId, Integer status, String category, String keyword, int currentPage, int pageSize);

    /**
     * 查询全部Prompt模板
     *
     * @param estabId  组织ID
     * @param status   状态 1启用 0停用
     * @param category 分类
     * @return Prompt模板列表
     */
    List<PromptTemplateEntity> listAllPromptTemplates(Long estabId, Integer status, String category);

    /**
     * 查询Prompt模板
     *
     * @param promptTemplateId Prompt模板ID
     * @return Prompt模板
     */
    PromptTemplateEntity findPromptTemplateById(Long promptTemplateId);

    /**
     * 统计Prompt模板编码数量
     *
     * @param estabId                 组织ID
     * @param promptCode              模板编码
     * @param excludePromptTemplateId 排除的模板ID
     * @return 模板编码数量
     */
    long countPromptTemplateCode(Long estabId, String promptCode, Long excludePromptTemplateId);

    /**
     * 插入Prompt模板
     *
     * @param promptTemplate Prompt模板
     * @return 插入的Prompt模板
     */
    PromptTemplateEntity insertPromptTemplate(PromptTemplateEntity promptTemplate);

    /**
     * 更新Prompt模板
     *
     * @param promptTemplate Prompt模板
     */
    void updatePromptTemplate(PromptTemplateEntity promptTemplate);

    /**
     * 删除Prompt模板
     *
     * @param promptTemplateId Prompt模板ID
     */
    void deletePromptTemplateById(Long promptTemplateId);

    /**
     * 统计引用指定Prompt模板的技能数量
     *
     * @param promptTemplateId Prompt模板ID
     * @return 技能数量
     */
    long countSkillsByPromptTemplateId(Long promptTemplateId);

    // ── ModelProvision ──

    /**
     * 查询租户模型开通分页列表
     *
     * @param estabId     组织ID
     * @param modelId     模型ID
     * @param status      状态 1启用 0停用
     * @param currentPage 当前页码
     * @param pageSize    每页数量
     * @return 租户模型开通分页列表
     */
    PageResponse<ModelProvisionEntity> listModelProvisions(Long estabId, Long modelId, Integer status, int currentPage, int pageSize);

    /**
     * 查询租户模型开通
     *
     * @param provisionId 开通ID
     * @return 租户模型开通
     */
    ModelProvisionEntity findModelProvisionById(Long provisionId);

    /**
     * 统计租户模型开通数量（唯一性校验）
     *
     * @param estabId   组织ID
     * @param modelId   模型ID
     * @param excludeId 排除的ID
     * @return 数量
     */
    long countModelProvision(Long estabId, Long modelId, Long excludeId);

    /**
     * 插入租户模型开通
     *
     * @param provision 租户模型开通
     * @return 插入的租户模型开通
     */
    ModelProvisionEntity insertModelProvision(ModelProvisionEntity provision);

    /**
     * 更新租户模型开通
     *
     * @param provision 租户模型开通
     */
    void updateModelProvision(ModelProvisionEntity provision);

    /**
     * 删除租户模型开通
     *
     * @param provisionId 开通ID
     */
    void deleteModelProvisionById(Long provisionId);

    /**
     * 查询租户的活跃模型开通（estabId + modelId, status=1, deleted=0）
     *
     * @param estabId 组织ID
     * @param modelId 模型ID
     * @return 租户模型开通实体，不存在返回 null
     */
    ModelProvisionEntity findActiveProvision(Long estabId, Long modelId);

    /**
     * 查询租户的默认模型开通（is_default=1, status=1, deleted=0）
     *
     * @param estabId 组织ID
     * @return 租户默认模型开通实体，不存在返回 null
     */
    ModelProvisionEntity findDefaultProvision(Long estabId);

    /**
     * 查询租户指定类型的默认模型开通（is_default=1, status=1, deleted=0, model_type匹配）
     *
     * @param estabId   组织ID
     * @param modelType 模型类型 1聊天 2嵌入 3图像生成 4语音转文字 5文字转语音 6重排序
     * @return 租户指定类型的默认模型开通实体，不存在返回 null
     */
    ModelProvisionEntity findDefaultProvisionByType(Long estabId, Integer modelType);

    // ── Tool ──

    /**
     * 查询工具分页列表
     *
     * @param estabId     组织ID
     * @param toolType    工具类型(FUNCTION/MCP/HTTP)
     * @param status      状态 1启用 0停用
     * @param keyword     搜索关键词
     * @param currentPage 当前页码
     * @param pageSize    每页数量
     * @return 工具分页列表
     */
    PageResponse<ToolEntity> listTools(Long estabId, String toolType, Integer status, String keyword, int currentPage, int pageSize);

    /**
     * 查询全部工具
     *
     * @param estabId  组织ID
     * @param toolType 工具类型(FUNCTION/MCP/HTTP)
     * @param status   状态 1启用 0停用
     * @return 工具列表
     */
    List<ToolEntity> listAllTools(Long estabId, String toolType, Integer status);

    /**
     * 查询工具
     *
     * @param toolId 工具ID
     * @return 工具
     */
    ToolEntity findToolById(Long toolId);

    /**
     * 统计工具编码数量
     *
     * @param estabId       组织ID
     * @param toolCode      工具编码
     * @param excludeToolId 排除的工具ID
     * @return 工具编码数量
     */
    long countToolCode(Long estabId, String toolCode, Long excludeToolId);

    /**
     * 插入工具
     *
     * @param tool 工具
     * @return 插入的工具
     */
    ToolEntity insertTool(ToolEntity tool);

    /**
     * 更新工具
     *
     * @param tool 工具
     */
    void updateTool(ToolEntity tool);

    /**
     * 删除工具
     *
     * @param toolId 工具ID
     */
    void deleteToolById(Long toolId);

    /**
     * 统计引用指定工具的技能工具关联数量
     *
     * @param toolId 工具ID
     * @return 技能工具关联数量
     */
    long countSkillToolsByToolId(Long toolId);

    // ── McpServer ──

    /**
     * 查询MCP服务器分页列表
     *
     * @param estabId       组织ID
     * @param transportType 传输类型(stdio/sse)
     * @param status        状态 1启用 0停用
     * @param keyword       搜索关键词
     * @param currentPage   当前页码
     * @param pageSize      每页数量
     * @return MCP服务器分页列表
     */
    PageResponse<McpServerEntity> listMcpServers(Long estabId, String transportType, Integer status, String keyword, int currentPage, int pageSize);

    /**
     * 查询全部MCP服务器
     *
     * @param estabId       组织ID
     * @param transportType 传输类型(stdio/sse)
     * @param status        状态 1启用 0停用
     * @return MCP服务器列表
     */
    List<McpServerEntity> listAllMcpServers(Long estabId, String transportType, Integer status);

    /**
     * 查询MCP服务器
     *
     * @param mcpServerId MCP服务器ID
     * @return MCP服务器
     */
    McpServerEntity findMcpServerById(Long mcpServerId);

    /**
     * 统计MCP服务器编码数量
     *
     * @param estabId            组织ID
     * @param serverCode         服务器编码
     * @param excludeMcpServerId 排除的MCP服务器ID
     * @return MCP服务器编码数量
     */
    long countMcpServerCode(Long estabId, String serverCode, Long excludeMcpServerId);

    /**
     * 插入MCP服务器
     *
     * @param mcpServer MCP服务器
     * @return 插入的MCP服务器
     */
    McpServerEntity insertMcpServer(McpServerEntity mcpServer);

    /**
     * 更新MCP服务器
     *
     * @param mcpServer MCP服务器
     */
    void updateMcpServer(McpServerEntity mcpServer);

    /**
     * 删除MCP服务器
     *
     * @param mcpServerId MCP服务器ID
     */
    void deleteMcpServerById(Long mcpServerId);

    /**
     * 统计引用指定MCP服务器的工具数量（tool_type=MCP且handler_ref=serverId）
     *
     * @param mcpServerId MCP服务器ID
     * @return 工具数量
     */
    long countToolsByMcpServerId(Long mcpServerId);

    // ── Skill ──

    /**
     * 查询技能分页列表
     *
     * @param estabId     组织ID
     * @param status      状态 1启用 0停用
     * @param keyword     搜索关键词
     * @param currentPage 当前页码
     * @param pageSize    每页数量
     * @return 技能分页列表
     */
    PageResponse<SkillEntity> listSkills(Long estabId, Integer status, String keyword, int currentPage, int pageSize);

    /**
     * 查询全部技能
     *
     * @param estabId 组织ID
     * @param status  状态 1启用 0停用
     * @return 技能列表
     */
    List<SkillEntity> listAllSkills(Long estabId, Integer status);

    /**
     * 查询技能
     *
     * @param skillId 技能ID
     * @return 技能
     */
    SkillEntity findSkillById(Long skillId);

    /**
     * 统计技能编码数量
     *
     * @param estabId        组织ID
     * @param skillCode      技能编码
     * @param excludeSkillId 排除的技能ID
     * @return 技能编码数量
     */
    long countSkillCode(Long estabId, String skillCode, Long excludeSkillId);

    /**
     * 插入技能
     *
     * @param skill 技能
     * @return 插入的技能
     */
    SkillEntity insertSkill(SkillEntity skill);

    /**
     * 更新技能
     *
     * @param skill 技能
     */
    void updateSkill(SkillEntity skill);

    /**
     * 删除技能
     *
     * @param skillId 技能ID
     */
    void deleteSkillById(Long skillId);

    // ── SkillTool ──

    /**
     * 查询技能关联的工具ID列表
     *
     * @param skillId 技能ID
     * @return 技能工具关联列表
     */
    List<SkillToolEntity> listSkillToolsBySkillId(Long skillId);

    /**
     * 全量替换技能工具关联
     *
     * @param skillId    技能ID
     * @param skillTools 技能工具关联列表
     */
    void replaceSkillTools(Long skillId, List<SkillToolEntity> skillTools);

    /**
     * 删除技能的所有工具关联
     *
     * @param skillId 技能ID
     */
    void deleteSkillToolsBySkillId(Long skillId);

    // ── Conversation ──

    /**
     * 根据会话唯一标识查询对话
     *
     * @param conversationId 会话唯一标识(UUID)
     * @return 对话实体，不存在返回 null
     */
    ConversationEntity findConversationByConversationId(String conversationId);

    /**
     * 查询对话分页列表
     *
     * @param estabId     组织ID
     * @param userId      用户ID
     * @param status      状态 1进行中 2已归档
     * @param currentPage 当前页码
     * @param pageSize    每页数量
     * @return 对话分页列表
     */
    PageResponse<ConversationEntity> listConversations(Long estabId, Long userId, Integer status, int currentPage, int pageSize);

    /**
     * 插入对话
     *
     * @param conversation 对话实体
     * @return 插入的对话实体
     */
    ConversationEntity insertConversation(ConversationEntity conversation);

    /**
     * 更新对话
     *
     * @param conversation 对话实体
     */
    void updateConversation(ConversationEntity conversation);

    /**
     * 根据会话唯一标识删除对话（逻辑删除）
     *
     * @param conversationId 会话唯一标识(UUID)
     */
    void deleteConversationByConversationId(String conversationId);

    // ── KnowledgeBase ──

    /**
     * 查询知识库分页列表
     *
     * @param estabId     组织ID
     * @param status      状态 1启用 0停用
     * @param keyword     搜索关键词
     * @param currentPage 当前页码
     * @param pageSize    每页数量
     * @return 知识库分页列表
     */
    PageResponse<KnowledgeBaseEntity> listKnowledgeBases(Long estabId, Integer status, String keyword, int currentPage, int pageSize);

    /**
     * 查询全部知识库
     *
     * @param estabId 组织ID
     * @param status  状态 1启用 0停用
     * @return 知识库列表
     */
    List<KnowledgeBaseEntity> listAllKnowledgeBases(Long estabId, Integer status);

    /**
     * 查询知识库
     *
     * @param id 知识库ID
     * @return 知识库实体
     */
    KnowledgeBaseEntity findKnowledgeBaseById(Long id);

    /**
     * 统计知识库编码数量
     *
     * @param estabId   组织ID
     * @param kbCode    知识库编码
     * @param excludeId 排除的知识库ID
     * @return 知识库编码数量
     */
    long countKbCode(Long estabId, String kbCode, Long excludeId);

    /**
     * 插入知识库
     *
     * @param entity 知识库实体
     * @return 插入的知识库实体
     */
    KnowledgeBaseEntity insertKnowledgeBase(KnowledgeBaseEntity entity);

    /**
     * 更新知识库
     *
     * @param entity 知识库实体
     */
    void updateKnowledgeBase(KnowledgeBaseEntity entity);

    /**
     * 删除知识库
     *
     * @param id 知识库ID
     */
    void deleteKnowledgeBaseById(Long id);

    // ── Folder ──

    /**
     * 查询知识库下的全部目录
     *
     * @param knowledgeBaseId 知识库ID
     * @return 目录列表
     */
    List<FolderEntity> listFoldersByKnowledgeBaseId(Long knowledgeBaseId);

    /**
     * 查询目录
     *
     * @param id 目录ID
     * @return 目录实体
     */
    FolderEntity findFolderById(Long id);

    /**
     * 统计同级目录名称数量
     *
     * @param knowledgeBaseId 知识库ID
     * @param parentId        父目录ID
     * @param folderName      目录名称
     * @param excludeId       排除的目录ID
     * @return 目录名称数量
     */
    long countFolderName(Long knowledgeBaseId, Long parentId, String folderName, Long excludeId);

    /**
     * 插入目录
     *
     * @param entity 目录实体
     * @return 插入的目录实体
     */
    FolderEntity insertFolder(FolderEntity entity);

    /**
     * 更新目录
     *
     * @param entity 目录实体
     */
    void updateFolder(FolderEntity entity);

    /**
     * 删除目录
     *
     * @param id 目录ID
     */
    void deleteFolderById(Long id);

    /**
     * 统计子目录数量
     *
     * @param parentId 父目录ID
     * @return 子目录数量
     */
    long countFoldersByParentId(Long parentId);

    /**
     * 更新目录排序
     *
     * @param id   目录ID
     * @param sort 排序值
     */
    void updateFolderSort(Long id, Integer sort);

    // ── Document ──

    /**
     * 查询文档分页列表
     *
     * @param knowledgeBaseId 知识库ID
     * @param folderId        目录ID
     * @param status          状态 1正常 0禁用
     * @param keyword         搜索关键词
     * @param currentPage     当前页码
     * @param pageSize        每页数量
     * @return 文档分页列表
     */
    PageResponse<DocumentEntity> listDocuments(Long knowledgeBaseId, Long folderId, Integer status, String keyword, int currentPage, int pageSize);

    /**
     * 查询文档
     *
     * @param id 文档ID
     * @return 文档实体
     */
    DocumentEntity findDocumentById(Long id);

    /**
     * 统计同目录下文档名称数量
     *
     * @param knowledgeBaseId 知识库ID
     * @param folderId        目录ID
     * @param docName         文档名称
     * @param excludeId       排除的文档ID
     * @return 文档名称数量
     */
    long countDocumentName(Long knowledgeBaseId, Long folderId, String docName, Long excludeId);

    /**
     * 插入文档
     *
     * @param entity 文档实体
     * @return 插入的文档实体
     */
    DocumentEntity insertDocument(DocumentEntity entity);

    /**
     * 更新文档
     *
     * @param entity 文档实体
     */
    void updateDocument(DocumentEntity entity);

    /**
     * 删除文档
     *
     * @param id 文档ID
     */
    void deleteDocumentById(Long id);

    /**
     * 统计知识库下的文档数量
     *
     * @param knowledgeBaseId 知识库ID
     * @return 文档数量
     */
    long countDocumentsByKnowledgeBaseId(Long knowledgeBaseId);

    /**
     * 统计目录下的文档数量
     *
     * @param folderId 目录ID
     * @return 文档数量
     */
    long countDocumentsByFolderId(Long folderId);

    /**
     * 更新文档排序
     *
     * @param id   文档ID
     * @param sort 排序值
     */
    void updateDocumentSort(Long id, Integer sort);

    // ── DocumentChunk ──

    /**
     * 查询文档的全部切片
     *
     * @param documentId 文档ID
     * @return 切片列表
     */
    List<DocumentChunkEntity> listChunksByDocumentId(Long documentId);

    /**
     * 删除文档的全部切片
     *
     * @param documentId 文档ID
     */
    void deleteChunksByDocumentId(Long documentId);

    /**
     * 批量插入切片
     *
     * @param chunks 切片列表
     */
    void batchInsertChunks(List<DocumentChunkEntity> chunks);

    /**
     * 查询知识库下所有需要向量化的文档（vectorStatus != VECTORIZING，content 非空）
     *
     * @param knowledgeBaseId 知识库ID
     * @return 待向量化的文档列表
     */
    List<DocumentEntity> listDocumentsForVectorization(Long knowledgeBaseId);

    // ── SkillKnowledge ──

    /**
     * 查询技能关联的知识库列表
     *
     * @param skillId 技能ID
     * @return 技能知识库关联列表
     */
    List<SkillKnowledgeEntity> listSkillKnowledgesBySkillId(Long skillId);

    /**
     * 删除技能的所有知识库关联
     *
     * @param skillId 技能ID
     */
    void deleteSkillKnowledgesBySkillId(Long skillId);

    /**
     * 批量插入技能知识库关联
     *
     * @param list 技能知识库关联列表
     */
    void batchInsertSkillKnowledges(List<SkillKnowledgeEntity> list);

    // ── UsageLog ──

    /**
     * 插入调用日志
     *
     * @param usageLog 调用日志实体
     * @return 插入的调用日志实体
     */
    UsageLogEntity insertUsageLog(UsageLogEntity usageLog);
}
