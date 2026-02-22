package cn.refinex.ai.application.service;

import cn.refinex.ai.application.assembler.AiDomainAssembler;
import cn.refinex.ai.application.command.*;
import cn.refinex.ai.application.dto.*;
import cn.refinex.ai.domain.error.AiErrorCode;
import cn.refinex.ai.domain.model.entity.*;
import cn.refinex.ai.domain.model.enums.ProviderProtocol;
import cn.refinex.ai.domain.repository.AiRepository;
import cn.refinex.ai.infrastructure.ai.ChatModelRegistry;
import cn.refinex.base.config.RefinexCryptoProperties;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.PageResponse;
import cn.refinex.base.utils.AesUtils;
import cn.refinex.base.utils.PageUtils;
import cn.refinex.satoken.helper.LoginUserHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.getIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * AI 应用服务
 *
 * @author refinex
 */
@Service
@RequiredArgsConstructor
public class AiApplicationService {

    private final AiRepository aiRepository;
    private final AiDomainAssembler aiDomainAssembler;
    private final RefinexCryptoProperties cryptoProperties;
    private final ChatModelRegistry chatModelRegistry;

    // ══════════════════════════════════════
    // Provider（供应商）
    // ══════════════════════════════════════

    /**
     * 查询供应商分页列表
     *
     * @param command 查询供应商列表命令
     * @return 供应商分页列表
     */
    public PageResponse<ProviderDTO> listProviders(QueryProviderListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);

        PageResponse<ProviderEntity> entities = aiRepository.listProviders(
                command == null ? null : command.getStatus(),
                command == null ? null : command.getKeyword(),
                currentPage, pageSize
        );

        List<ProviderDTO> result = new ArrayList<>();
        for (ProviderEntity entity : entities.getData()) {
            result.add(aiDomainAssembler.toProviderDto(entity));
        }

        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询全部供应商（不分页，用于下拉选择）
     *
     * @param status 状态（0-禁用，1-启用）
     * @return 供应商列表
     */
    public List<ProviderDTO> listAllProviders(Integer status) {
        List<ProviderEntity> entities = aiRepository.listAllProviders(status);
        List<ProviderDTO> result = new ArrayList<>();
        for (ProviderEntity entity : entities) {
            result.add(aiDomainAssembler.toProviderDto(entity));
        }
        return result;
    }

    /**
     * 查询供应商详情
     *
     * @param providerId 供应商ID
     * @return 供应商详情
     */
    public ProviderDTO getProvider(Long providerId) {
        ProviderEntity provider = requireProvider(providerId);
        return aiDomainAssembler.toProviderDto(provider);
    }

    /**
     * 创建供应商
     *
     * @param command 创建供应商命令
     * @return 创建的供应商DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public ProviderDTO createProvider(CreateProviderCommand command) {
        if (command == null || isBlank(command.getProviderCode()) || isBlank(command.getProviderName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        String providerCode = command.getProviderCode().trim();
        if (aiRepository.countProviderCode(providerCode, null) > 0) {
            throw new BizException(AiErrorCode.PROVIDER_CODE_DUPLICATED);
        }

        ProviderEntity entity = new ProviderEntity();
        entity.setProviderCode(providerCode);
        entity.setProviderName(command.getProviderName().trim());
        entity.setProtocol(getIfNull(trimToNull(command.getProtocol()), ProviderProtocol.OPENAI.getCode()));
        entity.setBaseUrl(trimToNull(command.getBaseUrl()));
        entity.setIconUrl(trimToNull(command.getIconUrl()));
        entity.setStatus(getIfNull(command.getStatus(), 1));
        entity.setSort(getIfNull(command.getSort(), 0));
        entity.setRemark(trimToNull(command.getRemark()));
        entity.setExtJson(trimToNull(command.getExtJson()));

        ProviderEntity created = aiRepository.insertProvider(entity);
        return aiDomainAssembler.toProviderDto(created);
    }

    /**
     * 更新供应商
     *
     * @param command 更新供应商命令
     * @return 更新后的供应商DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public ProviderDTO updateProvider(UpdateProviderCommand command) {
        if (command == null || command.getProviderId() == null || isBlank(command.getProviderName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        ProviderEntity existing = requireProvider(command.getProviderId());
        existing.setProviderName(command.getProviderName().trim());
        existing.setProtocol(getIfNull(trimToNull(command.getProtocol()), existing.getProtocol()));
        existing.setBaseUrl(trimToNull(command.getBaseUrl()));
        existing.setIconUrl(trimToNull(command.getIconUrl()));
        existing.setStatus(getIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(getIfNull(command.getSort(), existing.getSort()));
        existing.setRemark(trimToNull(command.getRemark()));
        existing.setExtJson(trimToNull(command.getExtJson()));

        aiRepository.updateProvider(existing);
        chatModelRegistry.evictAll();
        return aiDomainAssembler.toProviderDto(requireProvider(existing.getId()));
    }

    /**
     * 删除供应商（逻辑删除）
     *
     * @param providerId 供应商ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteProvider(Long providerId) {
        requireProvider(providerId);
        // 检查是否还有关联模型
        if (aiRepository.countModelsByProviderId(providerId) > 0) {
            throw new BizException("该供应商下仍有关联模型，请先删除模型", AiErrorCode.INVALID_PARAM);
        }
        aiRepository.deleteProviderById(providerId);
        chatModelRegistry.evictAll();
    }

    // ══════════════════════════════════════
    // Model（模型）
    // ══════════════════════════════════════

    /**
     * 查询模型分页列表
     *
     * @param command 查询模型列表命令
     * @return 模型分页列表
     */
    public PageResponse<ModelDTO> listModels(QueryModelListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);

        PageResponse<ModelEntity> entities = aiRepository.listModels(
                command == null ? null : command.getProviderId(),
                command == null ? null : command.getModelType(),
                command == null ? null : command.getStatus(),
                command == null ? null : command.getKeyword(),
                currentPage, pageSize
        );

        List<ModelDTO> result = new ArrayList<>();
        for (ModelEntity entity : entities.getData()) {
            result.add(aiDomainAssembler.toModelDto(entity));
        }

        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询指定供应商下的全部模型（不分页）
     *
     * @param providerId 供应商ID
     * @return 模型列表
     */
    public List<ModelDTO> listModelsByProviderId(Long providerId) {
        List<ModelEntity> entities = aiRepository.listModelsByProviderId(providerId);
        List<ModelDTO> result = new ArrayList<>();
        for (ModelEntity entity : entities) {
            result.add(aiDomainAssembler.toModelDto(entity));
        }
        return result;
    }

    /**
     * 查询模型详情
     *
     * @param modelId 模型ID
     * @return 模型详情
     */
    public ModelDTO getModel(Long modelId) {
        ModelEntity model = requireModel(modelId);
        return aiDomainAssembler.toModelDto(model);
    }

    /**
     * 创建模型
     *
     * @param command 创建模型命令
     * @return 创建后的模型DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public ModelDTO createModel(CreateModelCommand command) {
        if (command == null || command.getProviderId() == null || isBlank(command.getModelCode()) || isBlank(command.getModelName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        // 校验供应商存在
        requireProvider(command.getProviderId());

        String modelCode = command.getModelCode().trim();
        if (aiRepository.countModelCode(command.getProviderId(), modelCode, null) > 0) {
            throw new BizException(AiErrorCode.MODEL_CODE_DUPLICATED);
        }

        ModelEntity entity = new ModelEntity();
        entity.setProviderId(command.getProviderId());
        entity.setModelCode(modelCode);
        entity.setModelName(command.getModelName().trim());
        entity.setModelType(getIfNull(command.getModelType(), 1));
        entity.setCapVision(getIfNull(command.getCapVision(), 0));
        entity.setCapToolCall(getIfNull(command.getCapToolCall(), 0));
        entity.setCapStructuredOutput(getIfNull(command.getCapStructuredOutput(), 0));
        entity.setCapStreaming(getIfNull(command.getCapStreaming(), 1));
        entity.setCapReasoning(getIfNull(command.getCapReasoning(), 0));
        entity.setMaxContextWindow(command.getMaxContextWindow());
        entity.setMaxOutputTokens(command.getMaxOutputTokens());
        entity.setInputPrice(command.getInputPrice());
        entity.setOutputPrice(command.getOutputPrice());
        entity.setStatus(getIfNull(command.getStatus(), 1));
        entity.setSort(getIfNull(command.getSort(), 0));
        entity.setRemark(trimToNull(command.getRemark()));
        entity.setExtJson(trimToNull(command.getExtJson()));

        ModelEntity created = aiRepository.insertModel(entity);
        return aiDomainAssembler.toModelDto(created);
    }

    /**
     * 更新模型
     *
     * @param command 更新模型命令
     * @return 更新后的模型DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public ModelDTO updateModel(UpdateModelCommand command) {
        if (command == null || command.getModelId() == null || isBlank(command.getModelName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        ModelEntity existing = requireModel(command.getModelId());
        existing.setModelName(command.getModelName().trim());
        existing.setModelType(getIfNull(command.getModelType(), existing.getModelType()));
        existing.setCapVision(getIfNull(command.getCapVision(), existing.getCapVision()));
        existing.setCapToolCall(getIfNull(command.getCapToolCall(), existing.getCapToolCall()));
        existing.setCapStructuredOutput(getIfNull(command.getCapStructuredOutput(), existing.getCapStructuredOutput()));
        existing.setCapStreaming(getIfNull(command.getCapStreaming(), existing.getCapStreaming()));
        existing.setCapReasoning(getIfNull(command.getCapReasoning(), existing.getCapReasoning()));
        existing.setMaxContextWindow(command.getMaxContextWindow());
        existing.setMaxOutputTokens(command.getMaxOutputTokens());
        existing.setInputPrice(command.getInputPrice());
        existing.setOutputPrice(command.getOutputPrice());
        existing.setStatus(getIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(getIfNull(command.getSort(), existing.getSort()));
        existing.setRemark(trimToNull(command.getRemark()));
        existing.setExtJson(trimToNull(command.getExtJson()));

        aiRepository.updateModel(existing);
        chatModelRegistry.evictAll();
        return aiDomainAssembler.toModelDto(requireModel(existing.getId()));
    }

    /**
     * 删除模型（逻辑删除）
     *
     * @param modelId 模型ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteModel(Long modelId) {
        requireModel(modelId);
        aiRepository.deleteModelById(modelId);
        chatModelRegistry.evictAll();
    }

    // ══════════════════════════════════════
    // PromptTemplate（Prompt模板）
    // ══════════════════════════════════════

    /**
     * 查询Prompt模板分页列表
     *
     * @param command 查询Prompt模板列表命令
     * @return Prompt模板分页列表
     */
    public PageResponse<PromptTemplateDTO> listPromptTemplates(QueryPromptTemplateListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);

        Long estabId = LoginUserHelper.getEstabId();

        PageResponse<PromptTemplateEntity> entities = aiRepository.listPromptTemplates(
                estabId,
                command == null ? null : command.getStatus(),
                command == null ? null : command.getCategory(),
                command == null ? null : command.getKeyword(),
                currentPage, pageSize
        );

        List<PromptTemplateDTO> result = new ArrayList<>();
        for (PromptTemplateEntity entity : entities.getData()) {
            result.add(aiDomainAssembler.toPromptTemplateDto(entity));
        }

        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询全部Prompt模板（不分页，用于下拉选择）
     *
     * @param status   状态（0-禁用，1-启用）
     * @param category 分类
     * @return Prompt模板列表
     */
    public List<PromptTemplateDTO> listAllPromptTemplates(Integer status, String category) {
        Long estabId = LoginUserHelper.getEstabId();
        List<PromptTemplateEntity> entities = aiRepository.listAllPromptTemplates(estabId, status, category);
        List<PromptTemplateDTO> result = new ArrayList<>();
        for (PromptTemplateEntity entity : entities) {
            result.add(aiDomainAssembler.toPromptTemplateDto(entity));
        }
        return result;
    }

    /**
     * 查询Prompt模板详情
     *
     * @param promptTemplateId Prompt模板ID
     * @return Prompt模板详情
     */
    public PromptTemplateDTO getPromptTemplate(Long promptTemplateId) {
        PromptTemplateEntity entity = requirePromptTemplate(promptTemplateId);
        return aiDomainAssembler.toPromptTemplateDto(entity);
    }

    /**
     * 创建Prompt模板
     *
     * @param command 创建Prompt模板命令
     * @return 创建的Prompt模板DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public PromptTemplateDTO createPromptTemplate(CreatePromptTemplateCommand command) {
        if (command == null || isBlank(command.getPromptCode()) || isBlank(command.getPromptName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        Long estabId = LoginUserHelper.getEstabId();
        String promptCode = command.getPromptCode().trim();

        if (aiRepository.countPromptTemplateCode(estabId, promptCode, null) > 0) {
            throw new BizException(AiErrorCode.PROMPT_TEMPLATE_CODE_DUPLICATED);
        }

        PromptTemplateEntity entity = new PromptTemplateEntity();
        entity.setEstabId(estabId);
        entity.setPromptCode(promptCode);
        entity.setPromptName(command.getPromptName().trim());
        entity.setCategory(trimToNull(command.getCategory()));
        entity.setContent(command.getContent());
        entity.setVariables(trimToNull(command.getVariables()));
        entity.setVarOpen(getIfNull(trimToNull(command.getVarOpen()), "{{"));
        entity.setVarClose(getIfNull(trimToNull(command.getVarClose()), "}}"));
        entity.setLanguage(getIfNull(trimToNull(command.getLanguage()), "zh-CN"));
        entity.setIsBuiltin(getIfNull(command.getIsBuiltin(), 0));
        entity.setStatus(getIfNull(command.getStatus(), 1));
        entity.setSort(getIfNull(command.getSort(), 0));
        entity.setRemark(trimToNull(command.getRemark()));
        entity.setExtJson(trimToNull(command.getExtJson()));

        PromptTemplateEntity created = aiRepository.insertPromptTemplate(entity);
        return aiDomainAssembler.toPromptTemplateDto(created);
    }

    /**
     * 更新Prompt模板
     *
     * @param command 更新Prompt模板命令
     * @return 更新后的Prompt模板DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public PromptTemplateDTO updatePromptTemplate(UpdatePromptTemplateCommand command) {
        if (command == null || command.getPromptTemplateId() == null || isBlank(command.getPromptName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        PromptTemplateEntity existing = requirePromptTemplate(command.getPromptTemplateId());
        existing.setPromptName(command.getPromptName().trim());
        existing.setCategory(trimToNull(command.getCategory()));
        existing.setContent(command.getContent());
        existing.setVariables(trimToNull(command.getVariables()));
        existing.setVarOpen(getIfNull(trimToNull(command.getVarOpen()), existing.getVarOpen()));
        existing.setVarClose(getIfNull(trimToNull(command.getVarClose()), existing.getVarClose()));
        existing.setLanguage(getIfNull(trimToNull(command.getLanguage()), existing.getLanguage()));
        existing.setIsBuiltin(getIfNull(command.getIsBuiltin(), existing.getIsBuiltin()));
        existing.setStatus(getIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(getIfNull(command.getSort(), existing.getSort()));
        existing.setRemark(trimToNull(command.getRemark()));
        existing.setExtJson(trimToNull(command.getExtJson()));

        aiRepository.updatePromptTemplate(existing);
        return aiDomainAssembler.toPromptTemplateDto(requirePromptTemplate(existing.getId()));
    }

    /**
     * 删除Prompt模板（逻辑删除，检查Skill引用）
     *
     * @param promptTemplateId Prompt模板ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePromptTemplate(Long promptTemplateId) {
        requirePromptTemplate(promptTemplateId);
        if (aiRepository.countSkillsByPromptTemplateId(promptTemplateId) > 0) {
            throw new BizException("该Prompt模板仍被技能引用，请先解除关联", AiErrorCode.INVALID_PARAM);
        }
        aiRepository.deletePromptTemplateById(promptTemplateId);
    }

    // ══════════════════════════════════════
    // ModelProvision（租户模型开通）
    // ══════════════════════════════════════

    /**
     * 查询租户模型开通分页列表
     *
     * @param command 查询租户模型开通列表命令
     * @return 租户模型开通分页列表
     */
    public PageResponse<ModelProvisionDTO> listModelProvisions(QueryModelProvisionListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);

        PageResponse<ModelProvisionEntity> entities = aiRepository.listModelProvisions(
                command == null ? null : command.getEstabId(),
                command == null ? null : command.getModelId(),
                command == null ? null : command.getStatus(),
                currentPage, pageSize
        );

        List<ModelProvisionDTO> result = new ArrayList<>();
        for (ModelProvisionEntity entity : entities.getData()) {
            result.add(aiDomainAssembler.toModelProvisionDto(entity));
        }

        enrichModelProvisionDtos(result);

        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询租户模型开通详情
     *
     * @param provisionId 开通ID
     * @return 租户模型开通详情
     */
    public ModelProvisionDTO getModelProvision(Long provisionId) {
        ModelProvisionEntity entity = requireModelProvision(provisionId);
        ModelProvisionDTO dto = aiDomainAssembler.toModelProvisionDto(entity);
        enrichModelProvisionDtos(List.of(dto));
        return dto;
    }

    /**
     * 创建租户模型开通
     *
     * @param command 创建租户模型开通命令
     * @return 创建的租户模型开通DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public ModelProvisionDTO createModelProvision(CreateModelProvisionCommand command) {
        if (command == null || command.getModelId() == null) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        Long estabId = getIfNull(command.getEstabId(), 0L);

        // 校验模型存在且启用
        ModelEntity model = requireModel(command.getModelId());
        if (model.getStatus() != null && model.getStatus() == 0) {
            throw new BizException("关联的模型已停用", AiErrorCode.INVALID_PARAM);
        }

        // 校验唯一性：estabId + modelId
        if (aiRepository.countModelProvision(estabId, command.getModelId(), null) > 0) {
            throw new BizException(AiErrorCode.MODEL_PROVISION_DUPLICATED);
        }

        ModelProvisionEntity entity = new ModelProvisionEntity();
        entity.setEstabId(estabId);
        entity.setModelId(command.getModelId());
        entity.setApiKeyCipher(encryptApiKey(command.getApiKey()));
        entity.setApiBaseUrl(trimToNull(command.getApiBaseUrl()));
        entity.setDailyQuota(command.getDailyQuota());
        entity.setMonthlyQuota(command.getMonthlyQuota());
        entity.setIsDefault(getIfNull(command.getIsDefault(), 0));
        entity.setStatus(getIfNull(command.getStatus(), 1));
        entity.setRemark(trimToNull(command.getRemark()));
        entity.setExtJson(trimToNull(command.getExtJson()));

        ModelProvisionEntity created = aiRepository.insertModelProvision(entity);
        ModelProvisionDTO dto = aiDomainAssembler.toModelProvisionDto(created);
        enrichModelProvisionDtos(List.of(dto));
        return dto;
    }

    /**
     * 更新租户模型开通
     *
     * @param command 更新租户模型开通命令
     * @return 更新后的租户模型开通DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public ModelProvisionDTO updateModelProvision(UpdateModelProvisionCommand command) {
        if (command == null || command.getProvisionId() == null) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        ModelProvisionEntity existing = requireModelProvision(command.getProvisionId());

        // apiKey 为 null 表示不修改
        if (command.getApiKey() != null) {
            existing.setApiKeyCipher(encryptApiKey(command.getApiKey()));
        }
        existing.setApiBaseUrl(trimToNull(command.getApiBaseUrl()));
        existing.setDailyQuota(command.getDailyQuota());
        existing.setMonthlyQuota(command.getMonthlyQuota());
        existing.setIsDefault(getIfNull(command.getIsDefault(), existing.getIsDefault()));
        existing.setStatus(getIfNull(command.getStatus(), existing.getStatus()));
        existing.setRemark(trimToNull(command.getRemark()));
        existing.setExtJson(trimToNull(command.getExtJson()));

        aiRepository.updateModelProvision(existing);
        chatModelRegistry.evict(existing.getId());
        ModelProvisionDTO dto = aiDomainAssembler.toModelProvisionDto(requireModelProvision(existing.getId()));
        enrichModelProvisionDtos(List.of(dto));
        return dto;
    }

    /**
     * 删除租户模型开通（逻辑删除）
     *
     * @param provisionId 开通ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteModelProvision(Long provisionId) {
        requireModelProvision(provisionId);
        aiRepository.deleteModelProvisionById(provisionId);
        chatModelRegistry.evict(provisionId);
    }

    // ══════════════════════════════════════
    // Tool（工具）
    // ══════════════════════════════════════

    /**
     * 查询工具分页列表
     *
     * @param command 查询工具列表命令
     * @return 工具分页列表
     */
    public PageResponse<ToolDTO> listTools(QueryToolListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);

        Long estabId = LoginUserHelper.getEstabId();

        PageResponse<ToolEntity> entities = aiRepository.listTools(
                estabId,
                command == null ? null : command.getToolType(),
                command == null ? null : command.getStatus(),
                command == null ? null : command.getKeyword(),
                currentPage, pageSize
        );

        List<ToolDTO> result = new ArrayList<>();
        for (ToolEntity entity : entities.getData()) {
            result.add(aiDomainAssembler.toToolDto(entity));
        }

        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询全部工具（不分页，用于下拉选择）
     *
     * @param toolType 工具类型
     * @param status   状态（0-禁用，1-启用）
     * @return 工具列表
     */
    public List<ToolDTO> listAllTools(String toolType, Integer status) {
        Long estabId = LoginUserHelper.getEstabId();
        List<ToolEntity> entities = aiRepository.listAllTools(estabId, toolType, status);
        List<ToolDTO> result = new ArrayList<>();
        for (ToolEntity entity : entities) {
            result.add(aiDomainAssembler.toToolDto(entity));
        }
        return result;
    }

    /**
     * 查询工具详情
     *
     * @param toolId 工具ID
     * @return 工具详情
     */
    public ToolDTO getTool(Long toolId) {
        ToolEntity entity = requireTool(toolId);
        return aiDomainAssembler.toToolDto(entity);
    }

    /**
     * 创建工具
     *
     * @param command 创建工具命令
     * @return 创建的工具DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public ToolDTO createTool(CreateToolCommand command) {
        if (command == null || isBlank(command.getToolCode()) || isBlank(command.getToolName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        Long estabId = LoginUserHelper.getEstabId();
        String toolCode = command.getToolCode().trim();

        if (aiRepository.countToolCode(estabId, toolCode, null) > 0) {
            throw new BizException(AiErrorCode.TOOL_CODE_DUPLICATED);
        }

        ToolEntity entity = new ToolEntity();
        entity.setEstabId(estabId);
        entity.setToolCode(toolCode);
        entity.setToolName(command.getToolName().trim());
        entity.setToolType(trimToNull(command.getToolType()));
        entity.setDescription(trimToNull(command.getDescription()));
        entity.setInputSchema(trimToNull(command.getInputSchema()));
        entity.setOutputSchema(trimToNull(command.getOutputSchema()));
        entity.setHandlerRef(trimToNull(command.getHandlerRef()));
        entity.setRequireConfirm(getIfNull(command.getRequireConfirm(), 0));
        entity.setIsBuiltin(getIfNull(command.getIsBuiltin(), 0));
        entity.setStatus(getIfNull(command.getStatus(), 1));
        entity.setSort(getIfNull(command.getSort(), 0));
        entity.setRemark(trimToNull(command.getRemark()));
        entity.setExtJson(trimToNull(command.getExtJson()));

        ToolEntity created = aiRepository.insertTool(entity);
        return aiDomainAssembler.toToolDto(created);
    }

    /**
     * 更新工具
     *
     * @param command 更新工具命令
     * @return 更新后的工具DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public ToolDTO updateTool(UpdateToolCommand command) {
        if (command == null || command.getToolId() == null || isBlank(command.getToolName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        ToolEntity existing = requireTool(command.getToolId());
        existing.setToolName(command.getToolName().trim());
        existing.setToolType(getIfNull(trimToNull(command.getToolType()), existing.getToolType()));
        existing.setDescription(trimToNull(command.getDescription()));
        existing.setInputSchema(trimToNull(command.getInputSchema()));
        existing.setOutputSchema(trimToNull(command.getOutputSchema()));
        existing.setHandlerRef(trimToNull(command.getHandlerRef()));
        existing.setRequireConfirm(getIfNull(command.getRequireConfirm(), existing.getRequireConfirm()));
        existing.setIsBuiltin(getIfNull(command.getIsBuiltin(), existing.getIsBuiltin()));
        existing.setStatus(getIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(getIfNull(command.getSort(), existing.getSort()));
        existing.setRemark(trimToNull(command.getRemark()));
        existing.setExtJson(trimToNull(command.getExtJson()));

        aiRepository.updateTool(existing);
        return aiDomainAssembler.toToolDto(requireTool(existing.getId()));
    }

    /**
     * 删除工具（逻辑删除，检查SkillTool引用）
     *
     * @param toolId 工具ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTool(Long toolId) {
        requireTool(toolId);
        if (aiRepository.countSkillToolsByToolId(toolId) > 0) {
            throw new BizException("该工具仍被技能引用，请先解除关联", AiErrorCode.INVALID_PARAM);
        }
        aiRepository.deleteToolById(toolId);
    }

    // ══════════════════════════════════════
    // McpServer（MCP服务器）
    // ══════════════════════════════════════

    /**
     * 查询MCP服务器分页列表
     *
     * @param command 查询MCP服务器列表命令
     * @return MCP服务器分页列表
     */
    public PageResponse<McpServerDTO> listMcpServers(QueryMcpServerListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);

        Long estabId = LoginUserHelper.getEstabId();

        PageResponse<McpServerEntity> entities = aiRepository.listMcpServers(
                estabId,
                command == null ? null : command.getTransportType(),
                command == null ? null : command.getStatus(),
                command == null ? null : command.getKeyword(),
                currentPage, pageSize
        );

        List<McpServerDTO> result = new ArrayList<>();
        for (McpServerEntity entity : entities.getData()) {
            result.add(aiDomainAssembler.toMcpServerDto(entity));
        }

        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询全部MCP服务器（不分页，用于下拉选择）
     *
     * @param transportType 传输类型
     * @param status        状态（0-禁用，1-启用）
     * @return MCP服务器列表
     */
    public List<McpServerDTO> listAllMcpServers(String transportType, Integer status) {
        Long estabId = LoginUserHelper.getEstabId();
        List<McpServerEntity> entities = aiRepository.listAllMcpServers(estabId, transportType, status);
        List<McpServerDTO> result = new ArrayList<>();
        for (McpServerEntity entity : entities) {
            result.add(aiDomainAssembler.toMcpServerDto(entity));
        }
        return result;
    }

    /**
     * 查询MCP服务器详情
     *
     * @param mcpServerId MCP服务器ID
     * @return MCP服务器详情
     */
    public McpServerDTO getMcpServer(Long mcpServerId) {
        McpServerEntity entity = requireMcpServer(mcpServerId);
        return aiDomainAssembler.toMcpServerDto(entity);
    }

    /**
     * 创建MCP服务器
     *
     * @param command 创建MCP服务器命令
     * @return 创建的MCP服务器DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public McpServerDTO createMcpServer(CreateMcpServerCommand command) {
        if (command == null || isBlank(command.getServerCode()) || isBlank(command.getServerName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        Long estabId = LoginUserHelper.getEstabId();
        String serverCode = command.getServerCode().trim();

        if (aiRepository.countMcpServerCode(estabId, serverCode, null) > 0) {
            throw new BizException(AiErrorCode.MCP_SERVER_CODE_DUPLICATED);
        }

        McpServerEntity entity = new McpServerEntity();
        entity.setEstabId(estabId);
        entity.setServerCode(serverCode);
        entity.setServerName(command.getServerName().trim());
        entity.setTransportType(trimToNull(command.getTransportType()));
        entity.setEndpointUrl(trimToNull(command.getEndpointUrl()));
        entity.setCommand(trimToNull(command.getCommand()));
        entity.setArgs(trimToNull(command.getArgs()));
        entity.setEnvVars(trimToNull(command.getEnvVars()));
        entity.setStatus(getIfNull(command.getStatus(), 1));
        entity.setSort(getIfNull(command.getSort(), 0));
        entity.setRemark(trimToNull(command.getRemark()));
        entity.setExtJson(trimToNull(command.getExtJson()));

        McpServerEntity created = aiRepository.insertMcpServer(entity);
        return aiDomainAssembler.toMcpServerDto(created);
    }

    /**
     * 更新MCP服务器
     *
     * @param command 更新MCP服务器命令
     * @return 更新后的MCP服务器DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public McpServerDTO updateMcpServer(UpdateMcpServerCommand command) {
        if (command == null || command.getMcpServerId() == null || isBlank(command.getServerName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        McpServerEntity existing = requireMcpServer(command.getMcpServerId());
        existing.setServerName(command.getServerName().trim());
        existing.setTransportType(getIfNull(trimToNull(command.getTransportType()), existing.getTransportType()));
        existing.setEndpointUrl(trimToNull(command.getEndpointUrl()));
        existing.setCommand(trimToNull(command.getCommand()));
        existing.setArgs(trimToNull(command.getArgs()));
        existing.setEnvVars(trimToNull(command.getEnvVars()));
        existing.setStatus(getIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(getIfNull(command.getSort(), existing.getSort()));
        existing.setRemark(trimToNull(command.getRemark()));
        existing.setExtJson(trimToNull(command.getExtJson()));

        aiRepository.updateMcpServer(existing);
        return aiDomainAssembler.toMcpServerDto(requireMcpServer(existing.getId()));
    }

    /**
     * 删除MCP服务器（逻辑删除，检查Tool引用）
     *
     * @param mcpServerId MCP服务器ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMcpServer(Long mcpServerId) {
        requireMcpServer(mcpServerId);
        if (aiRepository.countToolsByMcpServerId(mcpServerId) > 0) {
            throw new BizException("该MCP服务器仍被工具引用，请先解除关联", AiErrorCode.INVALID_PARAM);
        }
        aiRepository.deleteMcpServerById(mcpServerId);
    }

    // ══════════════════════════════════════
    // Skill（技能）+ SkillTool
    // ══════════════════════════════════════

    /**
     * 查询技能分页列表
     *
     * @param command 查询技能列表命令
     * @return 技能分页列表
     */
    public PageResponse<SkillDTO> listSkills(QuerySkillListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);

        Long estabId = LoginUserHelper.getEstabId();

        PageResponse<SkillEntity> entities = aiRepository.listSkills(
                estabId,
                command == null ? null : command.getStatus(),
                command == null ? null : command.getKeyword(),
                currentPage, pageSize
        );

        List<SkillDTO> result = new ArrayList<>();
        for (SkillEntity entity : entities.getData()) {
            SkillDTO dto = aiDomainAssembler.toSkillDto(entity);
            dto.setToolIds(loadToolIds(entity.getId()));
            dto.setKnowledgeBaseIds(loadKnowledgeBaseIds(entity.getId()));
            result.add(dto);
        }

        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询全部技能（不分页，用于下拉选择）
     *
     * @param status 状态（0-禁用，1-启用）
     * @return 技能列表
     */
    public List<SkillDTO> listAllSkills(Integer status) {
        Long estabId = LoginUserHelper.getEstabId();
        List<SkillEntity> entities = aiRepository.listAllSkills(estabId, status);
        List<SkillDTO> result = new ArrayList<>();
        for (SkillEntity entity : entities) {
            SkillDTO dto = aiDomainAssembler.toSkillDto(entity);
            dto.setToolIds(loadToolIds(entity.getId()));
            dto.setKnowledgeBaseIds(loadKnowledgeBaseIds(entity.getId()));
            result.add(dto);
        }
        return result;
    }

    /**
     * 查询技能详情（含toolIds和knowledgeBaseIds）
     *
     * @param skillId 技能ID
     * @return 技能详情
     */
    public SkillDTO getSkill(Long skillId) {
        SkillEntity entity = requireSkill(skillId);
        SkillDTO dto = aiDomainAssembler.toSkillDto(entity);
        dto.setToolIds(loadToolIds(entity.getId()));
        dto.setKnowledgeBaseIds(loadKnowledgeBaseIds(entity.getId()));
        return dto;
    }

    /**
     * 创建技能
     *
     * @param command 创建技能命令
     * @return 创建的技能DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public SkillDTO createSkill(CreateSkillCommand command) {
        if (command == null || isBlank(command.getSkillCode()) || isBlank(command.getSkillName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        Long estabId = LoginUserHelper.getEstabId();
        String skillCode = command.getSkillCode().trim();

        if (aiRepository.countSkillCode(estabId, skillCode, null) > 0) {
            throw new BizException(AiErrorCode.SKILL_CODE_DUPLICATED);
        }

        // 校验关联的模型存在（若非空）
        if (command.getModelId() != null) {
            requireModel(command.getModelId());
        }
        // 校验关联的Prompt模板存在（若非空）
        if (command.getPromptTemplateId() != null) {
            requirePromptTemplate(command.getPromptTemplateId());
        }

        SkillEntity entity = new SkillEntity();
        entity.setEstabId(estabId);
        entity.setSkillCode(skillCode);
        entity.setSkillName(command.getSkillName().trim());
        entity.setDescription(trimToNull(command.getDescription()));
        entity.setIcon(trimToNull(command.getIcon()));
        entity.setModelId(command.getModelId());
        entity.setPromptTemplateId(command.getPromptTemplateId());
        entity.setTemperature(command.getTemperature());
        entity.setTopP(command.getTopP());
        entity.setMaxTokens(command.getMaxTokens());
        entity.setIsBuiltin(getIfNull(command.getIsBuiltin(), 0));
        entity.setStatus(getIfNull(command.getStatus(), 1));
        entity.setSort(getIfNull(command.getSort(), 0));
        entity.setRemark(trimToNull(command.getRemark()));
        entity.setExtJson(trimToNull(command.getExtJson()));

        SkillEntity created = aiRepository.insertSkill(entity);

        // 处理工具关联
        if (command.getToolIds() != null && !command.getToolIds().isEmpty()) {
            saveSkillTools(created.getId(), command.getToolIds());
        }

        // 处理知识库关联
        if (command.getKnowledgeBaseIds() != null && !command.getKnowledgeBaseIds().isEmpty()) {
            saveSkillKnowledges(created.getId(), command.getKnowledgeBaseIds());
        }

        SkillDTO dto = aiDomainAssembler.toSkillDto(created);
        dto.setToolIds(loadToolIds(created.getId()));
        dto.setKnowledgeBaseIds(loadKnowledgeBaseIds(created.getId()));
        return dto;
    }

    /**
     * 更新技能
     *
     * @param command 更新技能命令
     * @return 更新后的技能DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public SkillDTO updateSkill(UpdateSkillCommand command) {
        if (command == null || command.getSkillId() == null || isBlank(command.getSkillName())) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        SkillEntity existing = requireSkill(command.getSkillId());

        // 校验关联的模型存在（若非空）
        if (command.getModelId() != null) {
            requireModel(command.getModelId());
        }
        // 校验关联的Prompt模板存在（若非空）
        if (command.getPromptTemplateId() != null) {
            requirePromptTemplate(command.getPromptTemplateId());
        }

        existing.setSkillName(command.getSkillName().trim());
        existing.setDescription(trimToNull(command.getDescription()));
        existing.setIcon(trimToNull(command.getIcon()));
        existing.setModelId(command.getModelId());
        existing.setPromptTemplateId(command.getPromptTemplateId());
        existing.setTemperature(command.getTemperature());
        existing.setTopP(command.getTopP());
        existing.setMaxTokens(command.getMaxTokens());
        existing.setIsBuiltin(getIfNull(command.getIsBuiltin(), existing.getIsBuiltin()));
        existing.setStatus(getIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(getIfNull(command.getSort(), existing.getSort()));
        existing.setRemark(trimToNull(command.getRemark()));
        existing.setExtJson(trimToNull(command.getExtJson()));

        aiRepository.updateSkill(existing);

        // 全量替换工具关联（toolIds 为 null 时不操作，为空列表时清空）
        if (command.getToolIds() != null) {
            saveSkillTools(existing.getId(), command.getToolIds());
        }

        // 全量替换知识库关联（knowledgeBaseIds 为 null 时不操作，为空列表时清空）
        if (command.getKnowledgeBaseIds() != null) {
            saveSkillKnowledges(existing.getId(), command.getKnowledgeBaseIds());
        }

        SkillEntity updated = requireSkill(existing.getId());
        SkillDTO dto = aiDomainAssembler.toSkillDto(updated);
        dto.setToolIds(loadToolIds(updated.getId()));
        dto.setKnowledgeBaseIds(loadKnowledgeBaseIds(updated.getId()));
        return dto;
    }

    /**
     * 删除技能（逻辑删除，级联删除SkillTool和SkillKnowledge）
     *
     * @param skillId 技能ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSkill(Long skillId) {
        requireSkill(skillId);
        aiRepository.deleteSkillToolsBySkillId(skillId);
        aiRepository.deleteSkillKnowledgesBySkillId(skillId);
        aiRepository.deleteSkillById(skillId);
    }

    // ══════════════════════════════════════
    // 内部辅助方法
    // ══════════════════════════════════════

    /**
     * 校验并返回供应商实体
     *
     * @param providerId 供应商ID
     * @return 供应商实体
     */
    private ProviderEntity requireProvider(Long providerId) {
        if (providerId == null) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        ProviderEntity provider = aiRepository.findProviderById(providerId);
        if (provider == null || (provider.getDeleted() != null && provider.getDeleted() == 1)) {
            throw new BizException(AiErrorCode.PROVIDER_NOT_FOUND);
        }

        return provider;
    }

    /**
     * 校验并返回模型实体
     *
     * @param modelId 模型ID
     * @return 模型实体
     */
    private ModelEntity requireModel(Long modelId) {
        if (modelId == null) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        ModelEntity model = aiRepository.findModelById(modelId);
        if (model == null || (model.getDeleted() != null && model.getDeleted() == 1)) {
            throw new BizException(AiErrorCode.MODEL_NOT_FOUND);
        }

        return model;
    }

    /**
     * 校验并返回Prompt模板实体
     *
     * @param promptTemplateId Prompt模板ID
     * @return Prompt模板实体
     */
    private PromptTemplateEntity requirePromptTemplate(Long promptTemplateId) {
        if (promptTemplateId == null) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        PromptTemplateEntity entity = aiRepository.findPromptTemplateById(promptTemplateId);
        if (entity == null || (entity.getDeleted() != null && entity.getDeleted() == 1)) {
            throw new BizException(AiErrorCode.PROMPT_TEMPLATE_NOT_FOUND);
        }

        return entity;
    }

    /**
     * 校验并返回租户模型开通实体
     *
     * @param provisionId 开通ID
     * @return 租户模型开通实体
     */
    private ModelProvisionEntity requireModelProvision(Long provisionId) {
        if (provisionId == null) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        ModelProvisionEntity entity = aiRepository.findModelProvisionById(provisionId);
        if (entity == null || (entity.getDeleted() != null && entity.getDeleted() == 1)) {
            throw new BizException(AiErrorCode.MODEL_PROVISION_NOT_FOUND);
        }

        return entity;
    }

    /**
     * 校验并返回工具实体
     *
     * @param toolId 工具ID
     * @return 工具实体
     */
    private ToolEntity requireTool(Long toolId) {
        if (toolId == null) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        ToolEntity entity = aiRepository.findToolById(toolId);
        if (entity == null || (entity.getDeleted() != null && entity.getDeleted() == 1)) {
            throw new BizException(AiErrorCode.TOOL_NOT_FOUND);
        }

        return entity;
    }

    /**
     * 校验并返回MCP服务器实体
     *
     * @param mcpServerId MCP服务器ID
     * @return MCP服务器实体
     */
    private McpServerEntity requireMcpServer(Long mcpServerId) {
        if (mcpServerId == null) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        McpServerEntity entity = aiRepository.findMcpServerById(mcpServerId);
        if (entity == null || (entity.getDeleted() != null && entity.getDeleted() == 1)) {
            throw new BizException(AiErrorCode.MCP_SERVER_NOT_FOUND);
        }

        return entity;
    }

    /**
     * 校验并返回技能实体
     *
     * @param skillId 技能ID
     * @return 技能实体
     */
    private SkillEntity requireSkill(Long skillId) {
        if (skillId == null) {
            throw new BizException(AiErrorCode.INVALID_PARAM);
        }

        SkillEntity entity = aiRepository.findSkillById(skillId);
        if (entity == null || (entity.getDeleted() != null && entity.getDeleted() == 1)) {
            throw new BizException(AiErrorCode.SKILL_NOT_FOUND);
        }

        return entity;
    }

    /**
     * 加载技能关联的工具ID列表
     *
     * @param skillId 技能ID
     * @return 工具ID列表
     */
    private List<Long> loadToolIds(Long skillId) {
        List<SkillToolEntity> skillTools = aiRepository.listSkillToolsBySkillId(skillId);
        List<Long> toolIds = new ArrayList<>();
        for (SkillToolEntity st : skillTools) {
            toolIds.add(st.getToolId());
        }
        return toolIds;
    }

    /**
     * 保存技能工具关联（全量替换）
     *
     * @param skillId 技能ID
     * @param toolIds 工具ID列表
     */
    private void saveSkillTools(Long skillId, List<Long> toolIds) {
        List<SkillToolEntity> skillTools = new ArrayList<>();
        for (int i = 0; i < toolIds.size(); i++) {
            SkillToolEntity st = new SkillToolEntity();
            st.setSkillId(skillId);
            st.setToolId(toolIds.get(i));
            st.setSort(i);
            skillTools.add(st);
        }
        aiRepository.replaceSkillTools(skillId, skillTools);
    }

    /**
     * 加载技能关联的知识库ID列表
     *
     * @param skillId 技能ID
     * @return 知识库ID列表
     */
    private List<Long> loadKnowledgeBaseIds(Long skillId) {
        List<SkillKnowledgeEntity> skillKnowledges = aiRepository.listSkillKnowledgesBySkillId(skillId);
        List<Long> knowledgeBaseIds = new ArrayList<>();
        for (SkillKnowledgeEntity sk : skillKnowledges) {
            knowledgeBaseIds.add(sk.getKnowledgeBaseId());
        }
        return knowledgeBaseIds;
    }

    /**
     * 保存技能知识库关联（先删后插）
     *
     * @param skillId          技能ID
     * @param knowledgeBaseIds 知识库ID列表
     */
    private void saveSkillKnowledges(Long skillId, List<Long> knowledgeBaseIds) {
        aiRepository.deleteSkillKnowledgesBySkillId(skillId);
        if (knowledgeBaseIds != null && !knowledgeBaseIds.isEmpty()) {
            List<SkillKnowledgeEntity> list = new ArrayList<>();
            for (int i = 0; i < knowledgeBaseIds.size(); i++) {
                SkillKnowledgeEntity sk = new SkillKnowledgeEntity();
                sk.setSkillId(skillId);
                sk.setKnowledgeBaseId(knowledgeBaseIds.get(i));
                sk.setSort(i);
                list.add(sk);
            }
            aiRepository.batchInsertSkillKnowledges(list);
        }
    }

    /**
     * API Key 加密
     *
     * @param apiKey API Key 明文
     * @return AES 加密后的 Base64 密文；null 或空字符串返回 null
     */
    private String encryptApiKey(String apiKey) {
        return AesUtils.encrypt(apiKey, cryptoProperties.getAesKey());
    }

    /**
     * API Key 解密
     *
     * @param cipher AES 加密后的 Base64 密文
     * @return 解密后的 API Key 明文；null 或空字符串返回 null
     */
    private String decryptApiKey(String cipher) {
        return AesUtils.decrypt(cipher, cryptoProperties.getAesKey());
    }

    /**
     * 批量填充租户模型开通DTO的关联字段（供应商ID、供应商编码、模型编码、模型名称）
     * <p>
     * 通过批量查询 Model 和 Provider，避免循环内逐条查库。
     *
     * @param dtos 租户模型开通DTO列表
     */
    private void enrichModelProvisionDtos(List<ModelProvisionDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        // 1. 收集所有 modelId，批量查询 Model
        Set<Long> modelIds = dtos.stream()
                .map(ModelProvisionDTO::getModelId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (modelIds.isEmpty()) {
            return;
        }

        Map<Long, ModelEntity> modelMap = aiRepository.findModelsByIds(modelIds).stream()
                .collect(Collectors.toMap(ModelEntity::getId, Function.identity()));

        // 2. 收集所有 providerId，批量查询 Provider
        Set<Long> providerIds = modelMap.values().stream()
                .map(ModelEntity::getProviderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, ProviderEntity> providerMap = providerIds.isEmpty()
                ? Map.of()
                : aiRepository.findProvidersByIds(providerIds).stream()
                        .collect(Collectors.toMap(ProviderEntity::getId, Function.identity()));

        // 3. 填充每个 DTO
        for (ModelProvisionDTO dto : dtos) {
            if (dto.getModelId() == null) {
                continue;
            }
            ModelEntity model = modelMap.get(dto.getModelId());
            if (model == null) {
                continue;
            }
            dto.setModelCode(model.getModelCode());
            dto.setModelName(model.getModelName());
            if (model.getProviderId() != null) {
                dto.setProviderId(model.getProviderId());
                ProviderEntity provider = providerMap.get(model.getProviderId());
                if (provider != null) {
                    dto.setProviderCode(provider.getProviderCode());
                }
            }
        }
    }
}
