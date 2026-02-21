package cn.refinex.ai.application.assembler;

import cn.refinex.ai.application.dto.ModelDTO;
import cn.refinex.ai.application.dto.ProviderDTO;
import cn.refinex.ai.domain.model.entity.ModelEntity;
import cn.refinex.ai.domain.model.entity.ProviderEntity;
import org.mapstruct.Mapper;

/**
 * AI 领域层组装器（Entity ↔ DTO）
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface AiDomainAssembler {

    /**
     * 将供应商实体转换为供应商 DTO
     *
     * @param providerEntity 供应商实体
     * @return 供应商 DTO
     */
    ProviderDTO toProviderDto(ProviderEntity providerEntity);

    /**
     * 将模型实体转换为模型 DTO
     *
     * @param modelEntity 模型实体
     * @return 模型 DTO
     */
    ModelDTO toModelDto(ModelEntity modelEntity);
}
