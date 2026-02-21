package cn.refinex.ai.application.service;

import cn.refinex.ai.application.assembler.AiDomainAssembler;
import cn.refinex.ai.application.command.*;
import cn.refinex.ai.application.dto.ModelDTO;
import cn.refinex.ai.application.dto.ProviderDTO;
import cn.refinex.ai.domain.error.AiErrorCode;
import cn.refinex.ai.domain.model.entity.ModelEntity;
import cn.refinex.ai.domain.model.entity.ProviderEntity;
import cn.refinex.ai.domain.repository.AiRepository;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.PageResponse;
import cn.refinex.base.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
        entity.setProtocol(getIfNull(trimToNull(command.getProtocol()), "openai"));
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
        if (command == null || command.getProviderId() == null
                || isBlank(command.getModelCode()) || isBlank(command.getModelName())) {
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
        existing.setMaxContextWindow(command.getMaxContextWindow());
        existing.setMaxOutputTokens(command.getMaxOutputTokens());
        existing.setInputPrice(command.getInputPrice());
        existing.setOutputPrice(command.getOutputPrice());
        existing.setStatus(getIfNull(command.getStatus(), existing.getStatus()));
        existing.setSort(getIfNull(command.getSort(), existing.getSort()));
        existing.setRemark(trimToNull(command.getRemark()));
        existing.setExtJson(trimToNull(command.getExtJson()));

        aiRepository.updateModel(existing);
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
}
