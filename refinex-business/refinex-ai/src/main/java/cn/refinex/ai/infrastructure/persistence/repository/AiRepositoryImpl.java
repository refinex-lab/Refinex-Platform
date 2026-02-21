package cn.refinex.ai.infrastructure.persistence.repository;

import cn.refinex.ai.domain.model.entity.ModelEntity;
import cn.refinex.ai.domain.model.entity.ProviderEntity;
import cn.refinex.ai.domain.repository.AiRepository;
import cn.refinex.ai.infrastructure.converter.ModelDoConverter;
import cn.refinex.ai.infrastructure.converter.ProviderDoConverter;
import cn.refinex.ai.infrastructure.persistence.dataobject.AiModelDo;
import cn.refinex.ai.infrastructure.persistence.dataobject.AiProviderDo;
import cn.refinex.ai.infrastructure.persistence.mapper.AiModelMapper;
import cn.refinex.ai.infrastructure.persistence.mapper.AiProviderMapper;
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
    private final ProviderDoConverter providerDoConverter;
    private final ModelDoConverter modelDoConverter;

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
}
