package cn.refinex.ai.infrastructure.persistence.repository;

import cn.refinex.ai.domain.model.entity.*;
import cn.refinex.ai.domain.repository.AiRepository;
import cn.refinex.ai.infrastructure.converter.*;
import cn.refinex.ai.infrastructure.persistence.dataobject.*;
import cn.refinex.ai.infrastructure.persistence.mapper.*;
import cn.refinex.base.response.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 模块仓储实现
 *
 * @author refinex
 */
@Repository
@RequiredArgsConstructor
public class AiRepositoryImpl implements AiRepository {

    private final AiProviderMapper aiProviderMapper;
    private final AiModelMapper aiModelMapper;
    private final AiPromptTemplateMapper aiPromptTemplateMapper;
    private final AiModelProvisionMapper aiModelProvisionMapper;
    private final AiToolMapper aiToolMapper;
    private final AiMcpServerMapper aiMcpServerMapper;
    private final AiSkillMapper aiSkillMapper;
    private final AiSkillToolMapper aiSkillToolMapper;
    private final AiConversationMapper aiConversationMapper;
    private final AiUsageLogMapper aiUsageLogMapper;
    private final KbKnowledgeBaseMapper kbKnowledgeBaseMapper;
    private final KbFolderMapper kbFolderMapper;
    private final KbDocumentMapper kbDocumentMapper;
    private final KbDocumentChunkMapper kbDocumentChunkMapper;
    private final AiSkillKnowledgeMapper aiSkillKnowledgeMapper;
    private final ProviderDoConverter providerDoConverter;
    private final ModelDoConverter modelDoConverter;
    private final PromptTemplateDoConverter promptTemplateDoConverter;
    private final ModelProvisionDoConverter modelProvisionDoConverter;
    private final ToolDoConverter toolDoConverter;
    private final McpServerDoConverter mcpServerDoConverter;
    private final SkillDoConverter skillDoConverter;
    private final SkillToolDoConverter skillToolDoConverter;
    private final ConversationDoConverter conversationDoConverter;
    private final UsageLogDoConverter usageLogDoConverter;
    private final KnowledgeBaseDoConverter knowledgeBaseDoConverter;
    private final FolderDoConverter folderDoConverter;
    private final DocumentDoConverter documentDoConverter;
    private final DocumentChunkDoConverter documentChunkDoConverter;
    private final SkillKnowledgeDoConverter skillKnowledgeDoConverter;

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
    @Override
    public PageResponse<ProviderEntity> listProviders(Integer status, String keyword, int currentPage, int pageSize) {
        LambdaQueryWrapper<AiProviderDo> query = Wrappers.lambdaQuery(AiProviderDo.class)
                .eq(AiProviderDo::getDeleted, 0)
                .orderByAsc(AiProviderDo::getSort, AiProviderDo::getId);

        if (status != null) {
            query.eq(AiProviderDo::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(w -> w
                    .like(AiProviderDo::getProviderCode, trimmed)
                    .or().like(AiProviderDo::getProviderName, trimmed));
        }

        Page<AiProviderDo> page = new Page<>(currentPage, pageSize);
        Page<AiProviderDo> rowsPage = aiProviderMapper.selectPage(page, query);

        List<ProviderEntity> result = new ArrayList<>();
        for (AiProviderDo row : rowsPage.getRecords()) {
            result.add(providerDoConverter.toEntity(row));
        }

        return PageResponse.of(result, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 查询全部供应商
     *
     * @param status 状态 1启用 0停用
     * @return 供应商列表
     */
    @Override
    public List<ProviderEntity> listAllProviders(Integer status) {
        LambdaQueryWrapper<AiProviderDo> query = Wrappers.lambdaQuery(AiProviderDo.class)
                .eq(AiProviderDo::getDeleted, 0)
                .orderByAsc(AiProviderDo::getSort, AiProviderDo::getId);

        if (status != null) {
            query.eq(AiProviderDo::getStatus, status);
        }

        List<AiProviderDo> rows = aiProviderMapper.selectList(query);
        List<ProviderEntity> result = new ArrayList<>();
        for (AiProviderDo row : rows) {
            result.add(providerDoConverter.toEntity(row));
        }

        return result;
    }

    /**
     * 根据ID查询供应商
     *
     * @param providerId 供应商ID
     * @return 供应商实体
     */
    @Override
    public ProviderEntity findProviderById(Long providerId) {
        AiProviderDo row = aiProviderMapper.selectById(providerId);
        return row == null ? null : providerDoConverter.toEntity(row);
    }

    /**
     * 批量查询供应商
     *
     * @param providerIds 供应商ID集合
     * @return 供应商实体列表
     */
    @Override
    public List<ProviderEntity> findProvidersByIds(java.util.Collection<Long> providerIds) {
        if (providerIds == null || providerIds.isEmpty()) {
            return List.of();
        }
        List<AiProviderDo> rows = aiProviderMapper.selectByIds(providerIds);
        List<ProviderEntity> result = new ArrayList<>();
        for (AiProviderDo row : rows) {
            result.add(providerDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 统计供应商编码数量
     *
     * @param providerCode      供应商编码
     * @param excludeProviderId 排除的供应商ID
     * @return 供应商编码数量
     */
    @Override
    public long countProviderCode(String providerCode, Long excludeProviderId) {
        LambdaQueryWrapper<AiProviderDo> query = Wrappers.lambdaQuery(AiProviderDo.class)
                .eq(AiProviderDo::getProviderCode, providerCode)
                .eq(AiProviderDo::getDeleted, 0);

        if (excludeProviderId != null) {
            query.ne(AiProviderDo::getId, excludeProviderId);
        }

        Long count = aiProviderMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 插入供应商
     *
     * @param provider 供应商
     * @return 插入的供应商
     */
    @Override
    public ProviderEntity insertProvider(ProviderEntity provider) {
        AiProviderDo row = providerDoConverter.toDo(provider);
        aiProviderMapper.insert(row);
        return providerDoConverter.toEntity(row);
    }

    /**
     * 更新供应商
     *
     * @param provider 供应商
     */
    @Override
    public void updateProvider(ProviderEntity provider) {
        AiProviderDo row = providerDoConverter.toDo(provider);
        aiProviderMapper.updateById(row);
    }

    /**
     * 删除供应商
     *
     * @param providerId 供应商ID
     */
    @Override
    public void deleteProviderById(Long providerId) {
        aiProviderMapper.deleteById(providerId);
    }

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
    @Override
    public PageResponse<ModelEntity> listModels(Long providerId, Integer modelType, Integer status, String keyword, int currentPage, int pageSize) {
        LambdaQueryWrapper<AiModelDo> query = Wrappers.lambdaQuery(AiModelDo.class)
                .eq(AiModelDo::getDeleted, 0)
                .orderByAsc(AiModelDo::getSort, AiModelDo::getId);

        if (providerId != null) {
            query.eq(AiModelDo::getProviderId, providerId);
        }
        if (modelType != null) {
            query.eq(AiModelDo::getModelType, modelType);
        }
        if (status != null) {
            query.eq(AiModelDo::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(w -> w
                    .like(AiModelDo::getModelCode, trimmed)
                    .or().like(AiModelDo::getModelName, trimmed));
        }

        Page<AiModelDo> page = new Page<>(currentPage, pageSize);
        Page<AiModelDo> rowsPage = aiModelMapper.selectPage(page, query);

        List<ModelEntity> result = new ArrayList<>();
        for (AiModelDo row : rowsPage.getRecords()) {
            result.add(modelDoConverter.toEntity(row));
        }

        return PageResponse.of(result, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 根据供应商ID查询模型列表
     *
     * @param providerId 供应商ID
     * @return 模型列表
     */
    @Override
    public List<ModelEntity> listModelsByProviderId(Long providerId) {
        List<AiModelDo> rows = aiModelMapper.selectList(
                Wrappers.lambdaQuery(AiModelDo.class)
                        .eq(AiModelDo::getProviderId, providerId)
                        .eq(AiModelDo::getDeleted, 0)
                        .orderByAsc(AiModelDo::getSort, AiModelDo::getId)
        );

        List<ModelEntity> result = new ArrayList<>();
        for (AiModelDo row : rows) {
            result.add(modelDoConverter.toEntity(row));
        }

        return result;
    }

    /**
     * 根据ID查询模型
     *
     * @param modelId 模型ID
     * @return 模型实体
     */
    @Override
    public ModelEntity findModelById(Long modelId) {
        AiModelDo row = aiModelMapper.selectById(modelId);
        return row == null ? null : modelDoConverter.toEntity(row);
    }

    /**
     * 批量查询模型
     *
     * @param modelIds 模型ID集合
     * @return 模型实体列表
     */
    @Override
    public List<ModelEntity> findModelsByIds(java.util.Collection<Long> modelIds) {
        if (modelIds == null || modelIds.isEmpty()) {
            return List.of();
        }
        List<AiModelDo> rows = aiModelMapper.selectByIds(modelIds);
        List<ModelEntity> result = new ArrayList<>();
        for (AiModelDo row : rows) {
            result.add(modelDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 统计模型编码数量
     *
     * @param providerId     供应商ID
     * @param modelCode      模型编码
     * @param excludeModelId 排除的模型ID
     * @return 模型编码数量
     */
    @Override
    public long countModelCode(Long providerId, String modelCode, Long excludeModelId) {
        LambdaQueryWrapper<AiModelDo> query = Wrappers.lambdaQuery(AiModelDo.class)
                .eq(AiModelDo::getProviderId, providerId)
                .eq(AiModelDo::getModelCode, modelCode)
                .eq(AiModelDo::getDeleted, 0);

        if (excludeModelId != null) {
            query.ne(AiModelDo::getId, excludeModelId);
        }

        Long count = aiModelMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 插入模型
     *
     * @param model 模型实体
     * @return 模型实体
     */
    @Override
    public ModelEntity insertModel(ModelEntity model) {
        AiModelDo row = modelDoConverter.toDo(model);
        aiModelMapper.insert(row);
        return modelDoConverter.toEntity(row);
    }

    /**
     * 更新模型
     *
     * @param model 模型实体
     */
    @Override
    public void updateModel(ModelEntity model) {
        AiModelDo row = modelDoConverter.toDo(model);
        aiModelMapper.updateById(row);
    }

    /**
     * 删除模型
     *
     * @param modelId 模型ID
     */
    @Override
    public void deleteModelById(Long modelId) {
        aiModelMapper.deleteById(modelId);
    }

    /**
     * 统计供应商下的模型数量
     *
     * @param providerId 供应商ID
     * @return 模型数量
     */
    @Override
    public long countModelsByProviderId(Long providerId) {
        Long count = aiModelMapper.selectCount(
                Wrappers.lambdaQuery(AiModelDo.class)
                        .eq(AiModelDo::getProviderId, providerId)
                        .eq(AiModelDo::getDeleted, 0)
        );
        return count == null ? 0L : count;
    }

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
    @Override
    public PageResponse<PromptTemplateEntity> listPromptTemplates(Long estabId, Integer status, String category, String keyword, int currentPage, int pageSize) {
        LambdaQueryWrapper<AiPromptTemplateDo> query = Wrappers.lambdaQuery(AiPromptTemplateDo.class)
                .eq(AiPromptTemplateDo::getDeleted, 0)
                .orderByAsc(AiPromptTemplateDo::getSort, AiPromptTemplateDo::getId);

        if (estabId != null) {
            query.and(w -> w.eq(AiPromptTemplateDo::getEstabId, 0).or().eq(AiPromptTemplateDo::getEstabId, estabId));
        }
        if (status != null) {
            query.eq(AiPromptTemplateDo::getStatus, status);
        }
        if (category != null && !category.isBlank()) {
            query.eq(AiPromptTemplateDo::getCategory, category.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(w -> w
                    .like(AiPromptTemplateDo::getPromptCode, trimmed)
                    .or().like(AiPromptTemplateDo::getPromptName, trimmed));
        }

        Page<AiPromptTemplateDo> page = new Page<>(currentPage, pageSize);
        Page<AiPromptTemplateDo> rowsPage = aiPromptTemplateMapper.selectPage(page, query);

        List<PromptTemplateEntity> result = new ArrayList<>();
        for (AiPromptTemplateDo row : rowsPage.getRecords()) {
            result.add(promptTemplateDoConverter.toEntity(row));
        }

        return PageResponse.of(result, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 查询全部Prompt模板
     *
     * @param estabId  组织ID
     * @param status   状态 1启用 0停用
     * @param category 分类
     * @return Prompt模板列表
     */
    @Override
    public List<PromptTemplateEntity> listAllPromptTemplates(Long estabId, Integer status, String category) {
        LambdaQueryWrapper<AiPromptTemplateDo> query = Wrappers.lambdaQuery(AiPromptTemplateDo.class)
                .eq(AiPromptTemplateDo::getDeleted, 0)
                .orderByAsc(AiPromptTemplateDo::getSort, AiPromptTemplateDo::getId);

        if (estabId != null) {
            query.and(w -> w.eq(AiPromptTemplateDo::getEstabId, 0).or().eq(AiPromptTemplateDo::getEstabId, estabId));
        }
        if (status != null) {
            query.eq(AiPromptTemplateDo::getStatus, status);
        }
        if (category != null && !category.isBlank()) {
            query.eq(AiPromptTemplateDo::getCategory, category.trim());
        }

        List<AiPromptTemplateDo> rows = aiPromptTemplateMapper.selectList(query);
        List<PromptTemplateEntity> result = new ArrayList<>();
        for (AiPromptTemplateDo row : rows) {
            result.add(promptTemplateDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 查询Prompt模板
     *
     * @param promptTemplateId Prompt模板ID
     * @return Prompt模板
     */
    @Override
    public PromptTemplateEntity findPromptTemplateById(Long promptTemplateId) {
        AiPromptTemplateDo row = aiPromptTemplateMapper.selectById(promptTemplateId);
        return row == null ? null : promptTemplateDoConverter.toEntity(row);
    }

    /**
     * 统计Prompt模板编码数量
     *
     * @param estabId                 组织ID
     * @param promptCode              模板编码
     * @param excludePromptTemplateId 排除的模板ID
     * @return 模板编码数量
     */
    @Override
    public long countPromptTemplateCode(Long estabId, String promptCode, Long excludePromptTemplateId) {
        LambdaQueryWrapper<AiPromptTemplateDo> query = Wrappers.lambdaQuery(AiPromptTemplateDo.class)
                .eq(AiPromptTemplateDo::getEstabId, estabId)
                .eq(AiPromptTemplateDo::getPromptCode, promptCode)
                .eq(AiPromptTemplateDo::getDeleted, 0);

        if (excludePromptTemplateId != null) {
            query.ne(AiPromptTemplateDo::getId, excludePromptTemplateId);
        }

        Long count = aiPromptTemplateMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 插入Prompt模板
     *
     * @param promptTemplate Prompt模板
     * @return 插入的Prompt模板
     */
    @Override
    public PromptTemplateEntity insertPromptTemplate(PromptTemplateEntity promptTemplate) {
        AiPromptTemplateDo row = promptTemplateDoConverter.toDo(promptTemplate);
        aiPromptTemplateMapper.insert(row);
        return promptTemplateDoConverter.toEntity(row);
    }

    /**
     * 更新Prompt模板
     *
     * @param promptTemplate Prompt模板
     */
    @Override
    public void updatePromptTemplate(PromptTemplateEntity promptTemplate) {
        AiPromptTemplateDo row = promptTemplateDoConverter.toDo(promptTemplate);
        aiPromptTemplateMapper.updateById(row);
    }

    /**
     * 删除Prompt模板
     *
     * @param promptTemplateId Prompt模板ID
     */
    @Override
    public void deletePromptTemplateById(Long promptTemplateId) {
        aiPromptTemplateMapper.deleteById(promptTemplateId);
    }

    /**
     * 统计引用指定Prompt模板的技能数量
     *
     * @param promptTemplateId Prompt模板ID
     * @return 技能数量
     */
    @Override
    public long countSkillsByPromptTemplateId(Long promptTemplateId) {
        Long count = aiSkillMapper.selectCount(
                Wrappers.lambdaQuery(AiSkillDo.class)
                        .eq(AiSkillDo::getPromptTemplateId, promptTemplateId)
                        .eq(AiSkillDo::getDeleted, 0)
        );
        return count == null ? 0L : count;
    }

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
    @Override
    public PageResponse<ModelProvisionEntity> listModelProvisions(Long estabId, Long modelId, Integer status, int currentPage, int pageSize) {
        LambdaQueryWrapper<AiModelProvisionDo> query = Wrappers.lambdaQuery(AiModelProvisionDo.class)
                .eq(AiModelProvisionDo::getDeleted, 0)
                .orderByDesc(AiModelProvisionDo::getId);

        if (estabId != null) {
            query.eq(AiModelProvisionDo::getEstabId, estabId);
        }
        if (modelId != null) {
            query.eq(AiModelProvisionDo::getModelId, modelId);
        }
        if (status != null) {
            query.eq(AiModelProvisionDo::getStatus, status);
        }

        Page<AiModelProvisionDo> page = new Page<>(currentPage, pageSize);
        Page<AiModelProvisionDo> rowsPage = aiModelProvisionMapper.selectPage(page, query);

        List<ModelProvisionEntity> result = new ArrayList<>();
        for (AiModelProvisionDo row : rowsPage.getRecords()) {
            result.add(modelProvisionDoConverter.toEntity(row));
        }

        return PageResponse.of(result, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 查询租户模型开通
     *
     * @param provisionId 开通ID
     * @return 租户模型开通
     */
    @Override
    public ModelProvisionEntity findModelProvisionById(Long provisionId) {
        AiModelProvisionDo row = aiModelProvisionMapper.selectById(provisionId);
        return row == null ? null : modelProvisionDoConverter.toEntity(row);
    }

    /**
     * 统计租户模型开通数量（唯一性校验）
     *
     * @param estabId   组织ID
     * @param modelId   模型ID
     * @param excludeId 排除的ID
     * @return 数量
     */
    @Override
    public long countModelProvision(Long estabId, Long modelId, Long excludeId) {
        LambdaQueryWrapper<AiModelProvisionDo> query = Wrappers.lambdaQuery(AiModelProvisionDo.class)
                .eq(AiModelProvisionDo::getEstabId, estabId)
                .eq(AiModelProvisionDo::getModelId, modelId)
                .eq(AiModelProvisionDo::getDeleted, 0);

        if (excludeId != null) {
            query.ne(AiModelProvisionDo::getId, excludeId);
        }

        Long count = aiModelProvisionMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 插入租户模型开通
     *
     * @param provision 租户模型开通
     * @return 插入的租户模型开通
     */
    @Override
    public ModelProvisionEntity insertModelProvision(ModelProvisionEntity provision) {
        AiModelProvisionDo row = modelProvisionDoConverter.toDo(provision);
        aiModelProvisionMapper.insert(row);
        return modelProvisionDoConverter.toEntity(row);
    }

    /**
     * 更新租户模型开通
     *
     * @param provision 租户模型开通
     */
    @Override
    public void updateModelProvision(ModelProvisionEntity provision) {
        AiModelProvisionDo row = modelProvisionDoConverter.toDo(provision);
        aiModelProvisionMapper.updateById(row);
    }

    /**
     * 删除租户模型开通
     *
     * @param provisionId 开通ID
     */
    @Override
    public void deleteModelProvisionById(Long provisionId) {
        aiModelProvisionMapper.deleteById(provisionId);
    }

    /**
     * 查询租户的活跃模型开通（estabId + modelId, status=1, deleted=0）
     *
     * @param estabId 组织ID
     * @param modelId 模型ID
     * @return 租户模型开通实体，不存在返回 null
     */
    @Override
    public ModelProvisionEntity findActiveProvision(Long estabId, Long modelId) {
        AiModelProvisionDo row = aiModelProvisionMapper.selectOne(
                Wrappers.lambdaQuery(AiModelProvisionDo.class)
                        .eq(AiModelProvisionDo::getEstabId, estabId)
                        .eq(AiModelProvisionDo::getModelId, modelId)
                        .eq(AiModelProvisionDo::getStatus, 1)
                        .eq(AiModelProvisionDo::getDeleted, 0)
                        .last("LIMIT 1")
        );
        return row == null ? null : modelProvisionDoConverter.toEntity(row);
    }

    /**
     * 查询租户的默认模型开通（is_default=1, status=1, deleted=0）
     *
     * @param estabId 组织ID
     * @return 租户默认模型开通实体，不存在返回 null
     */
    @Override
    public ModelProvisionEntity findDefaultProvision(Long estabId) {
        AiModelProvisionDo row = aiModelProvisionMapper.selectOne(
                Wrappers.lambdaQuery(AiModelProvisionDo.class)
                        .eq(AiModelProvisionDo::getEstabId, estabId)
                        .eq(AiModelProvisionDo::getIsDefault, 1)
                        .eq(AiModelProvisionDo::getStatus, 1)
                        .eq(AiModelProvisionDo::getDeleted, 0)
                        .last("LIMIT 1")
        );
        return row == null ? null : modelProvisionDoConverter.toEntity(row);
    }

    /**
     * 查询租户指定类型的默认模型开通（is_default=1, status=1, deleted=0, model_type匹配）
     * <p>
     * 需要关联 ai_model 表按 model_type 过滤，使用 inSql 子查询实现。
     *
     * @param estabId   组织ID
     * @param modelType 模型类型 1聊天 2嵌入 3图像生成 4语音转文字 5文字转语音 6重排序
     * @return 租户指定类型的默认模型开通实体，不存在返回 null
     */
    @Override
    public ModelProvisionEntity findDefaultProvisionByType(Long estabId, Integer modelType) {
        AiModelProvisionDo row = aiModelProvisionMapper.selectOne(
                Wrappers.lambdaQuery(AiModelProvisionDo.class)
                        .eq(AiModelProvisionDo::getEstabId, estabId)
                        .eq(AiModelProvisionDo::getIsDefault, 1)
                        .eq(AiModelProvisionDo::getStatus, 1)
                        .eq(AiModelProvisionDo::getDeleted, 0)
                        .inSql(AiModelProvisionDo::getModelId,
                                "SELECT id FROM ai_model WHERE model_type = " + modelType + " AND deleted = 0")
                        .last("LIMIT 1")
        );
        return row == null ? null : modelProvisionDoConverter.toEntity(row);
    }

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
    @Override
    public PageResponse<ToolEntity> listTools(Long estabId, String toolType, Integer status, String keyword, int currentPage, int pageSize) {
        LambdaQueryWrapper<AiToolDo> query = Wrappers.lambdaQuery(AiToolDo.class)
                .eq(AiToolDo::getDeleted, 0)
                .orderByAsc(AiToolDo::getSort, AiToolDo::getId);

        if (estabId != null) {
            query.and(w -> w.eq(AiToolDo::getEstabId, 0).or().eq(AiToolDo::getEstabId, estabId));
        }
        if (toolType != null && !toolType.isBlank()) {
            query.eq(AiToolDo::getToolType, toolType.trim());
        }
        if (status != null) {
            query.eq(AiToolDo::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(w -> w
                    .like(AiToolDo::getToolCode, trimmed)
                    .or().like(AiToolDo::getToolName, trimmed));
        }

        Page<AiToolDo> page = new Page<>(currentPage, pageSize);
        Page<AiToolDo> rowsPage = aiToolMapper.selectPage(page, query);

        List<ToolEntity> result = new ArrayList<>();
        for (AiToolDo row : rowsPage.getRecords()) {
            result.add(toolDoConverter.toEntity(row));
        }

        return PageResponse.of(result, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 查询全部工具
     *
     * @param estabId  组织ID
     * @param toolType 工具类型(FUNCTION/MCP/HTTP)
     * @param status   状态 1启用 0停用
     * @return 工具列表
     */
    @Override
    public List<ToolEntity> listAllTools(Long estabId, String toolType, Integer status) {
        LambdaQueryWrapper<AiToolDo> query = Wrappers.lambdaQuery(AiToolDo.class)
                .eq(AiToolDo::getDeleted, 0)
                .orderByAsc(AiToolDo::getSort, AiToolDo::getId);

        if (estabId != null) {
            query.and(w -> w.eq(AiToolDo::getEstabId, 0).or().eq(AiToolDo::getEstabId, estabId));
        }
        if (toolType != null && !toolType.isBlank()) {
            query.eq(AiToolDo::getToolType, toolType.trim());
        }
        if (status != null) {
            query.eq(AiToolDo::getStatus, status);
        }

        List<AiToolDo> rows = aiToolMapper.selectList(query);
        List<ToolEntity> result = new ArrayList<>();
        for (AiToolDo row : rows) {
            result.add(toolDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 查询工具
     *
     * @param toolId 工具ID
     * @return 工具
     */
    @Override
    public ToolEntity findToolById(Long toolId) {
        AiToolDo row = aiToolMapper.selectById(toolId);
        return row == null ? null : toolDoConverter.toEntity(row);
    }

    /**
     * 统计工具编码数量
     *
     * @param estabId       组织ID
     * @param toolCode      工具编码
     * @param excludeToolId 排除的工具ID
     * @return 工具编码数量
     */
    @Override
    public long countToolCode(Long estabId, String toolCode, Long excludeToolId) {
        LambdaQueryWrapper<AiToolDo> query = Wrappers.lambdaQuery(AiToolDo.class)
                .eq(AiToolDo::getEstabId, estabId)
                .eq(AiToolDo::getToolCode, toolCode)
                .eq(AiToolDo::getDeleted, 0);

        if (excludeToolId != null) {
            query.ne(AiToolDo::getId, excludeToolId);
        }

        Long count = aiToolMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 插入工具
     *
     * @param tool 工具
     * @return 插入的工具
     */
    @Override
    public ToolEntity insertTool(ToolEntity tool) {
        AiToolDo row = toolDoConverter.toDo(tool);
        aiToolMapper.insert(row);
        return toolDoConverter.toEntity(row);
    }

    /**
     * 更新工具
     *
     * @param tool 工具
     */
    @Override
    public void updateTool(ToolEntity tool) {
        AiToolDo row = toolDoConverter.toDo(tool);
        aiToolMapper.updateById(row);
    }

    /**
     * 删除工具
     *
     * @param toolId 工具ID
     */
    @Override
    public void deleteToolById(Long toolId) {
        aiToolMapper.deleteById(toolId);
    }

    /**
     * 统计引用指定工具的技能工具关联数量
     *
     * @param toolId 工具ID
     * @return 技能工具关联数量
     */
    @Override
    public long countSkillToolsByToolId(Long toolId) {
        Long count = aiSkillToolMapper.selectCount(
                Wrappers.lambdaQuery(AiSkillToolDo.class)
                        .eq(AiSkillToolDo::getToolId, toolId)
                        .eq(AiSkillToolDo::getDeleted, 0)
        );
        return count == null ? 0L : count;
    }

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
    @Override
    public PageResponse<McpServerEntity> listMcpServers(Long estabId, String transportType, Integer status, String keyword, int currentPage, int pageSize) {
        LambdaQueryWrapper<AiMcpServerDo> query = Wrappers.lambdaQuery(AiMcpServerDo.class)
                .eq(AiMcpServerDo::getDeleted, 0)
                .orderByAsc(AiMcpServerDo::getSort, AiMcpServerDo::getId);

        if (estabId != null) {
            query.and(w -> w.eq(AiMcpServerDo::getEstabId, 0).or().eq(AiMcpServerDo::getEstabId, estabId));
        }
        if (transportType != null && !transportType.isBlank()) {
            query.eq(AiMcpServerDo::getTransportType, transportType.trim());
        }
        if (status != null) {
            query.eq(AiMcpServerDo::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(w -> w
                    .like(AiMcpServerDo::getServerCode, trimmed)
                    .or().like(AiMcpServerDo::getServerName, trimmed));
        }

        Page<AiMcpServerDo> page = new Page<>(currentPage, pageSize);
        Page<AiMcpServerDo> rowsPage = aiMcpServerMapper.selectPage(page, query);

        List<McpServerEntity> result = new ArrayList<>();
        for (AiMcpServerDo row : rowsPage.getRecords()) {
            result.add(mcpServerDoConverter.toEntity(row));
        }

        return PageResponse.of(result, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 查询全部MCP服务器
     *
     * @param estabId       组织ID
     * @param transportType 传输类型(stdio/sse)
     * @param status        状态 1启用 0停用
     * @return MCP服务器列表
     */
    @Override
    public List<McpServerEntity> listAllMcpServers(Long estabId, String transportType, Integer status) {
        LambdaQueryWrapper<AiMcpServerDo> query = Wrappers.lambdaQuery(AiMcpServerDo.class)
                .eq(AiMcpServerDo::getDeleted, 0)
                .orderByAsc(AiMcpServerDo::getSort, AiMcpServerDo::getId);

        if (estabId != null) {
            query.and(w -> w.eq(AiMcpServerDo::getEstabId, 0).or().eq(AiMcpServerDo::getEstabId, estabId));
        }
        if (transportType != null && !transportType.isBlank()) {
            query.eq(AiMcpServerDo::getTransportType, transportType.trim());
        }
        if (status != null) {
            query.eq(AiMcpServerDo::getStatus, status);
        }

        List<AiMcpServerDo> rows = aiMcpServerMapper.selectList(query);
        List<McpServerEntity> result = new ArrayList<>();
        for (AiMcpServerDo row : rows) {
            result.add(mcpServerDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 查询MCP服务器
     *
     * @param mcpServerId MCP服务器ID
     * @return MCP服务器
     */
    @Override
    public McpServerEntity findMcpServerById(Long mcpServerId) {
        AiMcpServerDo row = aiMcpServerMapper.selectById(mcpServerId);
        return row == null ? null : mcpServerDoConverter.toEntity(row);
    }

    /**
     * 统计MCP服务器编码数量
     *
     * @param estabId            组织ID
     * @param serverCode         服务器编码
     * @param excludeMcpServerId 排除的MCP服务器ID
     * @return MCP服务器编码数量
     */
    @Override
    public long countMcpServerCode(Long estabId, String serverCode, Long excludeMcpServerId) {
        LambdaQueryWrapper<AiMcpServerDo> query = Wrappers.lambdaQuery(AiMcpServerDo.class)
                .eq(AiMcpServerDo::getEstabId, estabId)
                .eq(AiMcpServerDo::getServerCode, serverCode)
                .eq(AiMcpServerDo::getDeleted, 0);

        if (excludeMcpServerId != null) {
            query.ne(AiMcpServerDo::getId, excludeMcpServerId);
        }

        Long count = aiMcpServerMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 插入MCP服务器
     *
     * @param mcpServer MCP服务器
     * @return 插入的MCP服务器
     */
    @Override
    public McpServerEntity insertMcpServer(McpServerEntity mcpServer) {
        AiMcpServerDo row = mcpServerDoConverter.toDo(mcpServer);
        aiMcpServerMapper.insert(row);
        return mcpServerDoConverter.toEntity(row);
    }

    /**
     * 更新MCP服务器
     *
     * @param mcpServer MCP服务器
     */
    @Override
    public void updateMcpServer(McpServerEntity mcpServer) {
        AiMcpServerDo row = mcpServerDoConverter.toDo(mcpServer);
        aiMcpServerMapper.updateById(row);
    }

    /**
     * 删除MCP服务器
     *
     * @param mcpServerId MCP服务器ID
     */
    @Override
    public void deleteMcpServerById(Long mcpServerId) {
        aiMcpServerMapper.deleteById(mcpServerId);
    }

    /**
     * 统计引用指定MCP服务器的工具数量（tool_type=MCP且handler_ref=serverId）
     *
     * @param mcpServerId MCP服务器ID
     * @return 工具数量
     */
    @Override
    public long countToolsByMcpServerId(Long mcpServerId) {
        Long count = aiToolMapper.selectCount(
                Wrappers.lambdaQuery(AiToolDo.class)
                        .eq(AiToolDo::getToolType, "MCP")
                        .eq(AiToolDo::getHandlerRef, String.valueOf(mcpServerId))
                        .eq(AiToolDo::getDeleted, 0)
        );
        return count == null ? 0L : count;
    }

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
    @Override
    public PageResponse<SkillEntity> listSkills(Long estabId, Integer status, String keyword, int currentPage, int pageSize) {
        LambdaQueryWrapper<AiSkillDo> query = Wrappers.lambdaQuery(AiSkillDo.class)
                .eq(AiSkillDo::getDeleted, 0)
                .orderByAsc(AiSkillDo::getSort, AiSkillDo::getId);

        if (estabId != null) {
            query.and(w -> w.eq(AiSkillDo::getEstabId, 0).or().eq(AiSkillDo::getEstabId, estabId));
        }
        if (status != null) {
            query.eq(AiSkillDo::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(w -> w
                    .like(AiSkillDo::getSkillCode, trimmed)
                    .or().like(AiSkillDo::getSkillName, trimmed));
        }

        Page<AiSkillDo> page = new Page<>(currentPage, pageSize);
        Page<AiSkillDo> rowsPage = aiSkillMapper.selectPage(page, query);

        List<SkillEntity> result = new ArrayList<>();
        for (AiSkillDo row : rowsPage.getRecords()) {
            result.add(skillDoConverter.toEntity(row));
        }

        return PageResponse.of(result, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 查询全部技能
     *
     * @param estabId 组织ID
     * @param status  状态 1启用 0停用
     * @return 技能列表
     */
    @Override
    public List<SkillEntity> listAllSkills(Long estabId, Integer status) {
        LambdaQueryWrapper<AiSkillDo> query = Wrappers.lambdaQuery(AiSkillDo.class)
                .eq(AiSkillDo::getDeleted, 0)
                .orderByAsc(AiSkillDo::getSort, AiSkillDo::getId);

        if (estabId != null) {
            query.and(w -> w.eq(AiSkillDo::getEstabId, 0).or().eq(AiSkillDo::getEstabId, estabId));
        }
        if (status != null) {
            query.eq(AiSkillDo::getStatus, status);
        }

        List<AiSkillDo> rows = aiSkillMapper.selectList(query);
        List<SkillEntity> result = new ArrayList<>();
        for (AiSkillDo row : rows) {
            result.add(skillDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 查询技能
     *
     * @param skillId 技能ID
     * @return 技能
     */
    @Override
    public SkillEntity findSkillById(Long skillId) {
        AiSkillDo row = aiSkillMapper.selectById(skillId);
        return row == null ? null : skillDoConverter.toEntity(row);
    }

    /**
     * 统计技能编码数量
     *
     * @param estabId        组织ID
     * @param skillCode      技能编码
     * @param excludeSkillId 排除的技能ID
     * @return 技能编码数量
     */
    @Override
    public long countSkillCode(Long estabId, String skillCode, Long excludeSkillId) {
        LambdaQueryWrapper<AiSkillDo> query = Wrappers.lambdaQuery(AiSkillDo.class)
                .eq(AiSkillDo::getEstabId, estabId)
                .eq(AiSkillDo::getSkillCode, skillCode)
                .eq(AiSkillDo::getDeleted, 0);

        if (excludeSkillId != null) {
            query.ne(AiSkillDo::getId, excludeSkillId);
        }

        Long count = aiSkillMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 插入技能
     *
     * @param skill 技能
     * @return 插入的技能
     */
    @Override
    public SkillEntity insertSkill(SkillEntity skill) {
        AiSkillDo row = skillDoConverter.toDo(skill);
        aiSkillMapper.insert(row);
        return skillDoConverter.toEntity(row);
    }

    /**
     * 更新技能
     *
     * @param skill 技能
     */
    @Override
    public void updateSkill(SkillEntity skill) {
        AiSkillDo row = skillDoConverter.toDo(skill);
        aiSkillMapper.updateById(row);
    }

    /**
     * 删除技能
     *
     * @param skillId 技能ID
     */
    @Override
    public void deleteSkillById(Long skillId) {
        aiSkillMapper.deleteById(skillId);
    }

    // ── SkillTool ──

    /**
     * 查询技能关联的工具ID列表
     *
     * @param skillId 技能ID
     * @return 技能工具关联列表
     */
    @Override
    public List<SkillToolEntity> listSkillToolsBySkillId(Long skillId) {
        List<AiSkillToolDo> rows = aiSkillToolMapper.selectList(
                Wrappers.lambdaQuery(AiSkillToolDo.class)
                        .eq(AiSkillToolDo::getSkillId, skillId)
                        .eq(AiSkillToolDo::getDeleted, 0)
                        .orderByAsc(AiSkillToolDo::getSort, AiSkillToolDo::getId)
        );

        List<SkillToolEntity> result = new ArrayList<>();
        for (AiSkillToolDo row : rows) {
            result.add(skillToolDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 全量替换技能工具关联
     *
     * @param skillId    技能ID
     * @param skillTools 技能工具关联列表
     */
    @Override
    public void replaceSkillTools(Long skillId, List<SkillToolEntity> skillTools) {
        // 先删除旧关联
        deleteSkillToolsBySkillId(skillId);
        // 再批量插入新关联
        if (skillTools != null && !skillTools.isEmpty()) {
            for (SkillToolEntity st : skillTools) {
                AiSkillToolDo row = skillToolDoConverter.toDo(st);
                aiSkillToolMapper.insert(row);
            }
        }
    }

    /**
     * 删除技能的所有工具关联
     *
     * @param skillId 技能ID
     */
    @Override
    public void deleteSkillToolsBySkillId(Long skillId) {
        aiSkillToolMapper.delete(
                Wrappers.lambdaQuery(AiSkillToolDo.class)
                        .eq(AiSkillToolDo::getSkillId, skillId)
        );
    }

    // ── Conversation ──

    /**
     * 根据会话唯一标识查询对话
     *
     * @param conversationId 会话唯一标识(UUID)
     * @return 对话实体，不存在返回 null
     */
    @Override
    public ConversationEntity findConversationByConversationId(String conversationId) {
        AiConversationDo row = aiConversationMapper.selectOne(
                Wrappers.lambdaQuery(AiConversationDo.class)
                        .eq(AiConversationDo::getConversationId, conversationId)
                        .eq(AiConversationDo::getDeleted, 0)
                        .last("LIMIT 1")
        );
        return row == null ? null : conversationDoConverter.toEntity(row);
    }

    /**
     * 查询对话分页列表
     *
     * @param estabId     组织ID
     * @param userId      用户ID
     * @param currentPage 当前页码
     * @param pageSize    每页数量
     * @return 对话分页列表
     */
    @Override
    public PageResponse<ConversationEntity> listConversations(Long estabId, Long userId, int currentPage, int pageSize) {
        LambdaQueryWrapper<AiConversationDo> query = Wrappers.lambdaQuery(AiConversationDo.class)
                .eq(AiConversationDo::getEstabId, estabId)
                .eq(AiConversationDo::getUserId, userId)
                .eq(AiConversationDo::getDeleted, 0)
                .orderByDesc(AiConversationDo::getPinned, AiConversationDo::getGmtModified);

        Page<AiConversationDo> page = new Page<>(currentPage, pageSize);
        Page<AiConversationDo> rowsPage = aiConversationMapper.selectPage(page, query);

        List<ConversationEntity> result = new ArrayList<>();
        for (AiConversationDo row : rowsPage.getRecords()) {
            result.add(conversationDoConverter.toEntity(row));
        }

        return PageResponse.of(result, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 插入对话
     *
     * @param conversation 对话实体
     * @return 插入的对话实体
     */
    @Override
    public ConversationEntity insertConversation(ConversationEntity conversation) {
        AiConversationDo row = conversationDoConverter.toDo(conversation);
        aiConversationMapper.insert(row);
        return conversationDoConverter.toEntity(row);
    }

    /**
     * 更新对话
     *
     * @param conversation 对话实体
     */
    @Override
    public void updateConversation(ConversationEntity conversation) {
        AiConversationDo row = conversationDoConverter.toDo(conversation);
        aiConversationMapper.updateById(row);
    }

    /**
     * 根据会话唯一标识删除对话（逻辑删除）
     *
     * @param conversationId 会话唯一标识(UUID)
     */
    @Override
    public void deleteConversationByConversationId(String conversationId) {
        AiConversationDo row = aiConversationMapper.selectOne(
                Wrappers.lambdaQuery(AiConversationDo.class)
                        .eq(AiConversationDo::getConversationId, conversationId)
                        .eq(AiConversationDo::getDeleted, 0)
                        .last("LIMIT 1")
        );
        if (row != null) {
            aiConversationMapper.deleteById(row.getId());
        }
    }

    // ── UsageLog ──

    /**
     * 插入调用日志
     *
     * @param usageLog 调用日志实体
     * @return 插入的调用日志实体
     */
    @Override
    public UsageLogEntity insertUsageLog(UsageLogEntity usageLog) {
        AiUsageLogDo row = usageLogDoConverter.toDo(usageLog);
        aiUsageLogMapper.insert(row);
        return usageLogDoConverter.toEntity(row);
    }

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
    @Override
    public PageResponse<KnowledgeBaseEntity> listKnowledgeBases(Long estabId, Integer status, String keyword, int currentPage, int pageSize) {
        LambdaQueryWrapper<KbKnowledgeBaseDo> query = Wrappers.lambdaQuery(KbKnowledgeBaseDo.class)
                .eq(KbKnowledgeBaseDo::getDeleted, 0)
                .orderByAsc(KbKnowledgeBaseDo::getSort, KbKnowledgeBaseDo::getId);

        if (estabId != null) {
            query.and(w -> w.eq(KbKnowledgeBaseDo::getEstabId, 0).or().eq(KbKnowledgeBaseDo::getEstabId, estabId));
        }
        if (status != null) {
            query.eq(KbKnowledgeBaseDo::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.and(w -> w
                    .like(KbKnowledgeBaseDo::getKbCode, trimmed)
                    .or().like(KbKnowledgeBaseDo::getKbName, trimmed));
        }

        Page<KbKnowledgeBaseDo> page = new Page<>(currentPage, pageSize);
        Page<KbKnowledgeBaseDo> rowsPage = kbKnowledgeBaseMapper.selectPage(page, query);

        List<KnowledgeBaseEntity> result = new ArrayList<>();
        for (KbKnowledgeBaseDo row : rowsPage.getRecords()) {
            result.add(knowledgeBaseDoConverter.toEntity(row));
        }

        return PageResponse.of(result, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 查询全部知识库
     *
     * @param estabId 组织ID
     * @param status  状态 1启用 0停用
     * @return 知识库列表
     */
    @Override
    public List<KnowledgeBaseEntity> listAllKnowledgeBases(Long estabId, Integer status) {
        LambdaQueryWrapper<KbKnowledgeBaseDo> query = Wrappers.lambdaQuery(KbKnowledgeBaseDo.class)
                .eq(KbKnowledgeBaseDo::getDeleted, 0)
                .orderByAsc(KbKnowledgeBaseDo::getSort, KbKnowledgeBaseDo::getId);

        if (estabId != null) {
            query.and(w -> w.eq(KbKnowledgeBaseDo::getEstabId, 0).or().eq(KbKnowledgeBaseDo::getEstabId, estabId));
        }
        if (status != null) {
            query.eq(KbKnowledgeBaseDo::getStatus, status);
        }

        List<KbKnowledgeBaseDo> rows = kbKnowledgeBaseMapper.selectList(query);
        List<KnowledgeBaseEntity> result = new ArrayList<>();
        for (KbKnowledgeBaseDo row : rows) {
            result.add(knowledgeBaseDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 查询知识库
     *
     * @param id 知识库ID
     * @return 知识库实体
     */
    @Override
    public KnowledgeBaseEntity findKnowledgeBaseById(Long id) {
        KbKnowledgeBaseDo row = kbKnowledgeBaseMapper.selectById(id);
        return row == null ? null : knowledgeBaseDoConverter.toEntity(row);
    }

    /**
     * 统计知识库编码数量
     *
     * @param estabId   组织ID
     * @param kbCode    知识库编码
     * @param excludeId 排除的知识库ID
     * @return 知识库编码数量
     */
    @Override
    public long countKbCode(Long estabId, String kbCode, Long excludeId) {
        LambdaQueryWrapper<KbKnowledgeBaseDo> query = Wrappers.lambdaQuery(KbKnowledgeBaseDo.class)
                .eq(KbKnowledgeBaseDo::getEstabId, estabId)
                .eq(KbKnowledgeBaseDo::getKbCode, kbCode)
                .eq(KbKnowledgeBaseDo::getDeleted, 0);

        if (excludeId != null) {
            query.ne(KbKnowledgeBaseDo::getId, excludeId);
        }

        Long count = kbKnowledgeBaseMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 插入知识库
     *
     * @param entity 知识库实体
     * @return 插入的知识库实体
     */
    @Override
    public KnowledgeBaseEntity insertKnowledgeBase(KnowledgeBaseEntity entity) {
        KbKnowledgeBaseDo row = knowledgeBaseDoConverter.toDo(entity);
        kbKnowledgeBaseMapper.insert(row);
        return knowledgeBaseDoConverter.toEntity(row);
    }

    /**
     * 更新知识库
     *
     * @param entity 知识库实体
     */
    @Override
    public void updateKnowledgeBase(KnowledgeBaseEntity entity) {
        KbKnowledgeBaseDo row = knowledgeBaseDoConverter.toDo(entity);
        kbKnowledgeBaseMapper.updateById(row);
    }

    /**
     * 删除知识库
     *
     * @param id 知识库ID
     */
    @Override
    public void deleteKnowledgeBaseById(Long id) {
        kbKnowledgeBaseMapper.deleteById(id);
    }

    // ── Folder ──

    /**
     * 查询知识库下的全部目录
     *
     * @param knowledgeBaseId 知识库ID
     * @return 目录列表
     */
    @Override
    public List<FolderEntity> listFoldersByKnowledgeBaseId(Long knowledgeBaseId) {
        List<KbFolderDo> rows = kbFolderMapper.selectList(
                Wrappers.lambdaQuery(KbFolderDo.class)
                        .eq(KbFolderDo::getKnowledgeBaseId, knowledgeBaseId)
                        .eq(KbFolderDo::getDeleted, 0)
                        .orderByAsc(KbFolderDo::getSort, KbFolderDo::getId)
        );

        List<FolderEntity> result = new ArrayList<>();
        for (KbFolderDo row : rows) {
            result.add(folderDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 查询目录
     *
     * @param id 目录ID
     * @return 目录实体
     */
    @Override
    public FolderEntity findFolderById(Long id) {
        KbFolderDo row = kbFolderMapper.selectById(id);
        return row == null ? null : folderDoConverter.toEntity(row);
    }

    /**
     * 统计同级目录名称数量
     *
     * @param knowledgeBaseId 知识库ID
     * @param parentId        父目录ID
     * @param folderName      目录名称
     * @param excludeId       排除的目录ID
     * @return 目录名称数量
     */
    @Override
    public long countFolderName(Long knowledgeBaseId, Long parentId, String folderName, Long excludeId) {
        LambdaQueryWrapper<KbFolderDo> query = Wrappers.lambdaQuery(KbFolderDo.class)
                .eq(KbFolderDo::getKnowledgeBaseId, knowledgeBaseId)
                .eq(KbFolderDo::getParentId, parentId)
                .eq(KbFolderDo::getFolderName, folderName)
                .eq(KbFolderDo::getDeleted, 0);

        if (excludeId != null) {
            query.ne(KbFolderDo::getId, excludeId);
        }

        Long count = kbFolderMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 插入目录
     *
     * @param entity 目录实体
     * @return 插入的目录实体
     */
    @Override
    public FolderEntity insertFolder(FolderEntity entity) {
        KbFolderDo row = folderDoConverter.toDo(entity);
        kbFolderMapper.insert(row);
        return folderDoConverter.toEntity(row);
    }

    /**
     * 更新目录
     *
     * @param entity 目录实体
     */
    @Override
    public void updateFolder(FolderEntity entity) {
        KbFolderDo row = folderDoConverter.toDo(entity);
        kbFolderMapper.updateById(row);
    }

    /**
     * 删除目录
     *
     * @param id 目录ID
     */
    @Override
    public void deleteFolderById(Long id) {
        kbFolderMapper.deleteById(id);
    }

    /**
     * 统计子目录数量
     *
     * @param parentId 父目录ID
     * @return 子目录数量
     */
    @Override
    public long countFoldersByParentId(Long parentId) {
        Long count = kbFolderMapper.selectCount(
                Wrappers.lambdaQuery(KbFolderDo.class)
                        .eq(KbFolderDo::getParentId, parentId)
                        .eq(KbFolderDo::getDeleted, 0)
        );
        return count == null ? 0L : count;
    }

    /**
     * 更新目录排序
     *
     * @param id   目录ID
     * @param sort 排序值
     */
    @Override
    public void updateFolderSort(Long id, Integer sort) {
        KbFolderDo row = new KbFolderDo();
        row.setId(id);
        row.setSort(sort);
        kbFolderMapper.updateById(row);
    }

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
    @Override
    public PageResponse<DocumentEntity> listDocuments(Long knowledgeBaseId, Long folderId, Integer status, String keyword, int currentPage, int pageSize) {
        LambdaQueryWrapper<KbDocumentDo> query = Wrappers.lambdaQuery(KbDocumentDo.class)
                .eq(KbDocumentDo::getKnowledgeBaseId, knowledgeBaseId)
                .eq(KbDocumentDo::getDeleted, 0)
                .orderByAsc(KbDocumentDo::getSort, KbDocumentDo::getId);

        if (folderId != null) {
            query.eq(KbDocumentDo::getFolderId, folderId);
        }
        if (status != null) {
            query.eq(KbDocumentDo::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            String trimmed = keyword.trim();
            query.like(KbDocumentDo::getDocName, trimmed);
        }

        Page<KbDocumentDo> page = new Page<>(currentPage, pageSize);
        Page<KbDocumentDo> rowsPage = kbDocumentMapper.selectPage(page, query);

        List<DocumentEntity> result = new ArrayList<>();
        for (KbDocumentDo row : rowsPage.getRecords()) {
            result.add(documentDoConverter.toEntity(row));
        }

        return PageResponse.of(result, rowsPage.getTotal(), (int) rowsPage.getSize(), (int) rowsPage.getCurrent());
    }

    /**
     * 查询文档
     *
     * @param id 文档ID
     * @return 文档实体
     */
    @Override
    public DocumentEntity findDocumentById(Long id) {
        KbDocumentDo row = kbDocumentMapper.selectById(id);
        return row == null ? null : documentDoConverter.toEntity(row);
    }

    /**
     * 统计同目录下文档名称数量
     *
     * @param knowledgeBaseId 知识库ID
     * @param folderId        目录ID
     * @param docName         文档名称
     * @param excludeId       排除的文档ID
     * @return 文档名称数量
     */
    @Override
    public long countDocumentName(Long knowledgeBaseId, Long folderId, String docName, Long excludeId) {
        LambdaQueryWrapper<KbDocumentDo> query = Wrappers.lambdaQuery(KbDocumentDo.class)
                .eq(KbDocumentDo::getKnowledgeBaseId, knowledgeBaseId)
                .eq(KbDocumentDo::getFolderId, folderId)
                .eq(KbDocumentDo::getDocName, docName)
                .eq(KbDocumentDo::getDeleted, 0);

        if (excludeId != null) {
            query.ne(KbDocumentDo::getId, excludeId);
        }

        Long count = kbDocumentMapper.selectCount(query);
        return count == null ? 0L : count;
    }

    /**
     * 插入文档
     *
     * @param entity 文档实体
     * @return 插入的文档实体
     */
    @Override
    public DocumentEntity insertDocument(DocumentEntity entity) {
        KbDocumentDo row = documentDoConverter.toDo(entity);
        kbDocumentMapper.insert(row);
        return documentDoConverter.toEntity(row);
    }

    /**
     * 更新文档
     *
     * @param entity 文档实体
     */
    @Override
    public void updateDocument(DocumentEntity entity) {
        KbDocumentDo row = documentDoConverter.toDo(entity);
        kbDocumentMapper.updateById(row);
    }

    /**
     * 删除文档
     *
     * @param id 文档ID
     */
    @Override
    public void deleteDocumentById(Long id) {
        kbDocumentMapper.deleteById(id);
    }

    /**
     * 统计知识库下的文档数量
     *
     * @param knowledgeBaseId 知识库ID
     * @return 文档数量
     */
    @Override
    public long countDocumentsByKnowledgeBaseId(Long knowledgeBaseId) {
        Long count = kbDocumentMapper.selectCount(
                Wrappers.lambdaQuery(KbDocumentDo.class)
                        .eq(KbDocumentDo::getKnowledgeBaseId, knowledgeBaseId)
                        .eq(KbDocumentDo::getDeleted, 0)
        );
        return count == null ? 0L : count;
    }

    /**
     * 统计目录下的文档数量
     *
     * @param folderId 目录ID
     * @return 文档数量
     */
    @Override
    public long countDocumentsByFolderId(Long folderId) {
        Long count = kbDocumentMapper.selectCount(
                Wrappers.lambdaQuery(KbDocumentDo.class)
                        .eq(KbDocumentDo::getFolderId, folderId)
                        .eq(KbDocumentDo::getDeleted, 0)
        );
        return count == null ? 0L : count;
    }

    /**
     * 更新文档排序
     *
     * @param id   文档ID
     * @param sort 排序值
     */
    @Override
    public void updateDocumentSort(Long id, Integer sort) {
        KbDocumentDo row = new KbDocumentDo();
        row.setId(id);
        row.setSort(sort);
        kbDocumentMapper.updateById(row);
    }

    // ── DocumentChunk ──

    /**
     * 查询文档的全部切片
     *
     * @param documentId 文档ID
     * @return 切片列表
     */
    @Override
    public List<DocumentChunkEntity> listChunksByDocumentId(Long documentId) {
        List<KbDocumentChunkDo> rows = kbDocumentChunkMapper.selectList(
                Wrappers.lambdaQuery(KbDocumentChunkDo.class)
                        .eq(KbDocumentChunkDo::getDocumentId, documentId)
                        .eq(KbDocumentChunkDo::getDeleted, 0)
                        .orderByAsc(KbDocumentChunkDo::getChunkIndex)
        );

        List<DocumentChunkEntity> result = new ArrayList<>();
        for (KbDocumentChunkDo row : rows) {
            result.add(documentChunkDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 删除文档的全部切片
     *
     * @param documentId 文档ID
     */
    @Override
    public void deleteChunksByDocumentId(Long documentId) {
        kbDocumentChunkMapper.delete(
                Wrappers.lambdaQuery(KbDocumentChunkDo.class)
                        .eq(KbDocumentChunkDo::getDocumentId, documentId)
        );
    }

    /**
     * 批量插入切片
     *
     * @param chunks 切片列表
     */
    @Override
    public void batchInsertChunks(List<DocumentChunkEntity> chunks) {
        if (chunks != null && !chunks.isEmpty()) {
            for (DocumentChunkEntity chunk : chunks) {
                KbDocumentChunkDo row = documentChunkDoConverter.toDo(chunk);
                kbDocumentChunkMapper.insert(row);
            }
        }
    }

    /**
     * 查询知识库下所有需要向量化的文档（vectorStatus != VECTORIZING，content 非空）
     *
     * @param knowledgeBaseId 知识库ID
     * @return 待向量化的文档列表
     */
    @Override
    public List<DocumentEntity> listDocumentsForVectorization(Long knowledgeBaseId) {
        List<KbDocumentDo> rows = kbDocumentMapper.selectList(
                Wrappers.lambdaQuery(KbDocumentDo.class)
                        .eq(KbDocumentDo::getKnowledgeBaseId, knowledgeBaseId)
                        .eq(KbDocumentDo::getDeleted, 0)
                        .isNotNull(KbDocumentDo::getContent)
                        .ne(KbDocumentDo::getContent, "")
                        .ne(KbDocumentDo::getVectorStatus, 1)
                        .orderByAsc(KbDocumentDo::getId)
        );

        List<DocumentEntity> result = new ArrayList<>();
        for (KbDocumentDo row : rows) {
            result.add(documentDoConverter.toEntity(row));
        }
        return result;
    }

    // ── SkillKnowledge ──

    /**
     * 查询技能关联的知识库列表
     *
     * @param skillId 技能ID
     * @return 技能知识库关联列表
     */
    @Override
    public List<SkillKnowledgeEntity> listSkillKnowledgesBySkillId(Long skillId) {
        List<AiSkillKnowledgeDo> rows = aiSkillKnowledgeMapper.selectList(
                Wrappers.lambdaQuery(AiSkillKnowledgeDo.class)
                        .eq(AiSkillKnowledgeDo::getSkillId, skillId)
                        .eq(AiSkillKnowledgeDo::getDeleted, 0)
                        .orderByAsc(AiSkillKnowledgeDo::getSort, AiSkillKnowledgeDo::getId)
        );

        List<SkillKnowledgeEntity> result = new ArrayList<>();
        for (AiSkillKnowledgeDo row : rows) {
            result.add(skillKnowledgeDoConverter.toEntity(row));
        }
        return result;
    }

    /**
     * 删除技能的所有知识库关联
     *
     * @param skillId 技能ID
     */
    @Override
    public void deleteSkillKnowledgesBySkillId(Long skillId) {
        aiSkillKnowledgeMapper.delete(
                Wrappers.lambdaQuery(AiSkillKnowledgeDo.class)
                        .eq(AiSkillKnowledgeDo::getSkillId, skillId)
        );
    }

    /**
     * 批量插入技能知识库关联
     *
     * @param list 技能知识库关联列表
     */
    @Override
    public void batchInsertSkillKnowledges(List<SkillKnowledgeEntity> list) {
        if (list != null && !list.isEmpty()) {
            for (SkillKnowledgeEntity entity : list) {
                AiSkillKnowledgeDo row = skillKnowledgeDoConverter.toDo(entity);
                aiSkillKnowledgeMapper.insert(row);
            }
        }
    }
}
