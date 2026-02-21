package cn.refinex.ai.infrastructure.converter;

import cn.refinex.ai.domain.model.entity.McpServerEntity;
import cn.refinex.ai.infrastructure.persistence.dataobject.AiMcpServerDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MCP服务器 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface McpServerDoConverter {

    /**
     * 转换为MCP服务器实体
     *
     * @param mcpServerDo MCP服务器数据对象
     * @return MCP服务器实体
     */
    McpServerEntity toEntity(AiMcpServerDo mcpServerDo);

    /**
     * 转换为MCP服务器数据对象
     *
     * @param mcpServerEntity MCP服务器实体
     * @return MCP服务器数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    AiMcpServerDo toDo(McpServerEntity mcpServerEntity);
}
