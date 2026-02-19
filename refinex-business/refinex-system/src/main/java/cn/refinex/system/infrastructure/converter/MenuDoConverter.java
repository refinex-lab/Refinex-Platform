package cn.refinex.system.infrastructure.converter;

import cn.refinex.system.domain.model.entity.MenuEntity;
import cn.refinex.system.infrastructure.persistence.dataobject.ScrMenuDo;
import org.mapstruct.Mapper;

/**
 * 菜单 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface MenuDoConverter {

    /**
     * 转换为菜单实体
     *
     * @param menuDo 菜单数据对象
     * @return 菜单实体
     */
    MenuEntity toEntity(ScrMenuDo menuDo);
}
