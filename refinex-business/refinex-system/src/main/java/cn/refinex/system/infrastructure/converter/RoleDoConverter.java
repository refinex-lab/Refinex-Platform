package cn.refinex.system.infrastructure.converter;

import cn.refinex.system.domain.model.entity.RoleEntity;
import cn.refinex.system.infrastructure.persistence.dataobject.ScrRoleDo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 角色 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface RoleDoConverter {

    /**
     * 转换为角色实体
     *
     * @param roleDo 角色数据对象
     * @return 角色实体
     */
    RoleEntity toEntity(ScrRoleDo roleDo);

    /**
     * 转换为角色数据对象
     *
     * @param roleEntity 角色实体
     * @return 角色数据对象
     */
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleteBy", ignore = true)
    @Mapping(target = "lockVersion", ignore = true)
    @Mapping(target = "gmtCreate", ignore = true)
    @Mapping(target = "gmtModified", ignore = true)
    ScrRoleDo toDo(RoleEntity roleEntity);
}
