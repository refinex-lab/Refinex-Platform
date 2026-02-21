package cn.refinex.ai.interfaces.assembler;

import cn.refinex.ai.application.command.*;
import cn.refinex.ai.application.dto.ModelDTO;
import cn.refinex.ai.application.dto.ProviderDTO;
import cn.refinex.ai.interfaces.dto.*;
import cn.refinex.ai.interfaces.vo.ModelVO;
import cn.refinex.ai.interfaces.vo.ProviderVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * AI 接口层组装器（Request/Query ↔ Command，DTO → VO）
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface AiApiAssembler {

    // ── Provider ──

    /**
     * 查询供应商列表参数转换
     *
     * @param query 查询供应商列表参数
     * @return 查询供应商列表命令
     */
    QueryProviderListCommand toQueryProviderListCommand(ProviderListQuery query);

    /**
     * 创建供应商参数转换
     *
     * @param request 创建供应商参数
     * @return 创建供应商命令
     */
    CreateProviderCommand toCreateProviderCommand(ProviderCreateRequest request);

    /**
     * 更新供应商参数转换
     *
     * @param request 更新供应商参数
     * @return 更新供应商命令
     */
    @Mapping(target = "providerId", ignore = true)
    UpdateProviderCommand toUpdateProviderCommand(ProviderUpdateRequest request);

    /**
     * 供应商DTO转换为供应商VO
     *
     * @param dto 供应商DTO
     * @return 供应商VO
     */
    ProviderVO toProviderVo(ProviderDTO dto);

    /**
     * 供应商DTO列表转换为供应商VO列表
     *
     * @param dtos 供应商DTO列表
     * @return 供应商VO列表
     */
    List<ProviderVO> toProviderVoList(List<ProviderDTO> dtos);

    // ── Model ──

    /**
     * 查询模型列表参数转换
     *
     * @param query 查询模型列表参数
     * @return 查询模型列表命令
     */
    QueryModelListCommand toQueryModelListCommand(ModelListQuery query);

    /**
     * 创建模型参数转换
     *
     * @param request 创建模型参数
     * @return 创建模型命令
     */
    CreateModelCommand toCreateModelCommand(ModelCreateRequest request);

    /**
     * 更新模型参数转换
     *
     * @param request 更新模型参数
     * @return 更新模型命令
     */
    @Mapping(target = "modelId", ignore = true)
    UpdateModelCommand toUpdateModelCommand(ModelUpdateRequest request);

    /**
     * 模型DTO转换为模型VO
     *
     * @param dto 模型DTO
     * @return 模型VO
     */
    ModelVO toModelVo(ModelDTO dto);

    /**
     * 模型DTO列表转换为模型VO列表
     *
     * @param dtos 模型DTO列表
     * @return 模型VO列表
     */
    List<ModelVO> toModelVoList(List<ModelDTO> dtos);
}
