package cn.refinex.ai.domain.repository;

import cn.refinex.ai.domain.model.entity.ModelEntity;
import cn.refinex.ai.domain.model.entity.ProviderEntity;
import cn.refinex.base.response.PageResponse;

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
}
