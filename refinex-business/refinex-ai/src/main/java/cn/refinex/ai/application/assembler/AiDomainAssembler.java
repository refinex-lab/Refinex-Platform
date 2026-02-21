package cn.refinex.ai.application.assembler;

import cn.refinex.ai.application.dto.*;
import cn.refinex.ai.domain.model.entity.*;
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

    /**
     * 将Prompt模板实体转换为Prompt模板 DTO
     *
     * @param promptTemplateEntity Prompt模板实体
     * @return Prompt模板 DTO
     */
    PromptTemplateDTO toPromptTemplateDto(PromptTemplateEntity promptTemplateEntity);

    /**
     * 将租户模型开通实体转换为租户模型开通 DTO
     *
     * @param modelProvisionEntity 租户模型开通实体
     * @return 租户模型开通 DTO
     */
    ModelProvisionDTO toModelProvisionDto(ModelProvisionEntity modelProvisionEntity);

    /**
     * 将工具实体转换为工具 DTO
     *
     * @param toolEntity 工具实体
     * @return 工具 DTO
     */
    ToolDTO toToolDto(ToolEntity toolEntity);

    /**
     * 将MCP服务器实体转换为MCP服务器 DTO
     *
     * @param mcpServerEntity MCP服务器实体
     * @return MCP服务器 DTO
     */
    McpServerDTO toMcpServerDto(McpServerEntity mcpServerEntity);

    /**
     * 将技能实体转换为技能 DTO（不含 toolIds，需手动补充）
     *
     * @param skillEntity 技能实体
     * @return 技能 DTO
     */
    SkillDTO toSkillDto(SkillEntity skillEntity);
}
