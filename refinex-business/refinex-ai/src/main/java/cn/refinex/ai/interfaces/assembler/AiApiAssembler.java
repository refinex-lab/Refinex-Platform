package cn.refinex.ai.interfaces.assembler;

import cn.refinex.ai.application.command.*;
import cn.refinex.ai.application.dto.*;
import cn.refinex.ai.interfaces.dto.*;
import cn.refinex.ai.interfaces.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

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

    // ── PromptTemplate ──

    /**
     * 查询Prompt模板列表参数转换
     *
     * @param query 查询Prompt模板列表参数
     * @return 查询Prompt模板列表命令
     */
    QueryPromptTemplateListCommand toQueryPromptTemplateListCommand(PromptTemplateListQuery query);

    /**
     * 创建Prompt模板参数转换
     *
     * @param request 创建Prompt模板参数
     * @return 创建Prompt模板命令
     */
    CreatePromptTemplateCommand toCreatePromptTemplateCommand(PromptTemplateCreateRequest request);

    /**
     * 更新Prompt模板参数转换
     *
     * @param request 更新Prompt模板参数
     * @return 更新Prompt模板命令
     */
    @Mapping(target = "promptTemplateId", ignore = true)
    UpdatePromptTemplateCommand toUpdatePromptTemplateCommand(PromptTemplateUpdateRequest request);

    /**
     * Prompt模板DTO转换为Prompt模板VO
     *
     * @param dto Prompt模板DTO
     * @return Prompt模板VO
     */
    PromptTemplateVO toPromptTemplateVo(PromptTemplateDTO dto);

    /**
     * Prompt模板DTO列表转换为Prompt模板VO列表
     *
     * @param dtos Prompt模板DTO列表
     * @return Prompt模板VO列表
     */
    List<PromptTemplateVO> toPromptTemplateVoList(List<PromptTemplateDTO> dtos);

    // ── ModelProvision ──

    /**
     * 查询租户模型开通列表参数转换
     *
     * @param query 查询租户模型开通列表参数
     * @return 查询租户模型开通列表命令
     */
    QueryModelProvisionListCommand toQueryModelProvisionListCommand(ModelProvisionListQuery query);

    /**
     * 创建租户模型开通参数转换
     *
     * @param request 创建租户模型开通参数
     * @return 创建租户模型开通命令
     */
    CreateModelProvisionCommand toCreateModelProvisionCommand(ModelProvisionCreateRequest request);

    /**
     * 更新租户模型开通参数转换
     *
     * @param request 更新租户模型开通参数
     * @return 更新租户模型开通命令
     */
    @Mapping(target = "provisionId", ignore = true)
    UpdateModelProvisionCommand toUpdateModelProvisionCommand(ModelProvisionUpdateRequest request);

    /**
     * 租户模型开通DTO转换为租户模型开通VO（自定义apiKeyMasked映射）
     *
     * @param dto 租户模型开通DTO
     * @return 租户模型开通VO
     */
    @Mapping(target = "apiKeyMasked", source = "apiKeyCipher", qualifiedByName = "maskApiKey")
    ModelProvisionVO toModelProvisionVo(ModelProvisionDTO dto);

    /**
     * 租户模型开通DTO列表转换为租户模型开通VO列表
     *
     * @param dtos 租户模型开通DTO列表
     * @return 租户模型开通VO列表
     */
    List<ModelProvisionVO> toModelProvisionVoList(List<ModelProvisionDTO> dtos);

    // ── Tool ──

    /**
     * 查询工具列表参数转换
     *
     * @param query 查询工具列表参数
     * @return 查询工具列表命令
     */
    QueryToolListCommand toQueryToolListCommand(ToolListQuery query);

    /**
     * 创建工具参数转换
     *
     * @param request 创建工具参数
     * @return 创建工具命令
     */
    CreateToolCommand toCreateToolCommand(ToolCreateRequest request);

    /**
     * 更新工具参数转换
     *
     * @param request 更新工具参数
     * @return 更新工具命令
     */
    @Mapping(target = "toolId", ignore = true)
    UpdateToolCommand toUpdateToolCommand(ToolUpdateRequest request);

    /**
     * 工具DTO转换为工具VO
     *
     * @param dto 工具DTO
     * @return 工具VO
     */
    ToolVO toToolVo(ToolDTO dto);

    /**
     * 工具DTO列表转换为工具VO列表
     *
     * @param dtos 工具DTO列表
     * @return 工具VO列表
     */
    List<ToolVO> toToolVoList(List<ToolDTO> dtos);

    // ── McpServer ──

    /**
     * 查询MCP服务器列表参数转换
     *
     * @param query 查询MCP服务器列表参数
     * @return 查询MCP服务器列表命令
     */
    QueryMcpServerListCommand toQueryMcpServerListCommand(McpServerListQuery query);

    /**
     * 创建MCP服务器参数转换
     *
     * @param request 创建MCP服务器参数
     * @return 创建MCP服务器命令
     */
    CreateMcpServerCommand toCreateMcpServerCommand(McpServerCreateRequest request);

    /**
     * 更新MCP服务器参数转换
     *
     * @param request 更新MCP服务器参数
     * @return 更新MCP服务器命令
     */
    @Mapping(target = "mcpServerId", ignore = true)
    UpdateMcpServerCommand toUpdateMcpServerCommand(McpServerUpdateRequest request);

    /**
     * MCP服务器DTO转换为MCP服务器VO（自定义envVarsMasked映射）
     *
     * @param dto MCP服务器DTO
     * @return MCP服务器VO
     */
    @Mapping(target = "envVarsMasked", source = "envVars", qualifiedByName = "maskEnvVars")
    McpServerVO toMcpServerVo(McpServerDTO dto);

    /**
     * MCP服务器DTO列表转换为MCP服务器VO列表
     *
     * @param dtos MCP服务器DTO列表
     * @return MCP服务器VO列表
     */
    List<McpServerVO> toMcpServerVoList(List<McpServerDTO> dtos);

    // ── Skill ──

    /**
     * 查询技能列表参数转换
     *
     * @param query 查询技能列表参数
     * @return 查询技能列表命令
     */
    QuerySkillListCommand toQuerySkillListCommand(SkillListQuery query);

    /**
     * 创建技能参数转换
     *
     * @param request 创建技能参数
     * @return 创建技能命令
     */
    CreateSkillCommand toCreateSkillCommand(SkillCreateRequest request);

    /**
     * 更新技能参数转换
     *
     * @param request 更新技能参数
     * @return 更新技能命令
     */
    @Mapping(target = "skillId", ignore = true)
    UpdateSkillCommand toUpdateSkillCommand(SkillUpdateRequest request);

    /**
     * 技能DTO转换为技能VO
     *
     * @param dto 技能DTO
     * @return 技能VO
     */
    SkillVO toSkillVo(SkillDTO dto);

    /**
     * 技能DTO列表转换为技能VO列表
     *
     * @param dtos 技能DTO列表
     * @return 技能VO列表
     */
    List<SkillVO> toSkillVoList(List<SkillDTO> dtos);

    // ── 脱敏辅助方法 ──

    // ── Conversation ──

    /**
     * 对话请求转换为流式对话命令
     *
     * @param request 对话请求
     * @return 流式对话命令
     */
    @Mapping(target = "estabId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    StreamChatCommand toStreamChatCommand(ChatRequest request);

    /**
     * 对话列表查询参数转换为查询命令
     *
     * @param query 对话列表查询参数
     * @return 查询对话列表命令
     */
    @Mapping(target = "estabId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    QueryConversationListCommand toQueryConversationListCommand(ConversationListQuery query);

    /**
     * 对话DTO转换为对话VO
     *
     * @param dto 对话DTO
     * @return 对话VO
     */
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    ConversationVO toConversationVo(ConversationDTO dto);

    /**
     * 对话DTO列表转换为对话VO列表
     *
     * @param dtos 对话DTO列表
     * @return 对话VO列表
     */
    List<ConversationVO> toConversationVoList(List<ConversationDTO> dtos);

    /**
     * 对话DTO转换为对话详情VO
     *
     * @param dto 对话DTO
     * @return 对话详情VO
     */
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    @Mapping(target = "messages", ignore = true)
    ConversationDetailVO toConversationDetailVo(ConversationDTO dto);

    // ── 脱敏辅助方法 ──

    /**
     * API Key脱敏：非空时显示 ****xxxx（后4位），否则返回null
     *
     * @param apiKeyCipher API Key密文
     * @return 脱敏后的API Key
     */
    @Named("maskApiKey")
    default String maskApiKey(String apiKeyCipher) {
        if (apiKeyCipher == null || apiKeyCipher.isEmpty()) {
            return null;
        }
        if (apiKeyCipher.length() <= 4) {
            return "****" + apiKeyCipher;
        }
        return "****" + apiKeyCipher.substring(apiKeyCipher.length() - 4);
    }

    /**
     * 环境变量脱敏：解析JSON，保留key，value替换为***
     *
     * @param envVars 环境变量JSON字符串
     * @return 脱敏后的环境变量JSON字符串
     */
    @Named("maskEnvVars")
    default String maskEnvVars(String envVars) {
        if (envVars == null || envVars.isEmpty()) {
            return null;
        }
        try {
            // 简单的JSON key-value脱敏：将 "key":"value" 替换为 "key":"***"
            return envVars.replaceAll("\"([^\"]+)\"\\s*:\\s*\"[^\"]*\"", "\"$1\":\"***\"");
        } catch (Exception e) {
            return "***";
        }
    }
}
