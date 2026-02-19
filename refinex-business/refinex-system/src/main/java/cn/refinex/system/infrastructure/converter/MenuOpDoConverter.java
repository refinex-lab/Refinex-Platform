package cn.refinex.system.infrastructure.converter;

import cn.refinex.system.domain.model.entity.MenuOpEntity;
import cn.refinex.system.infrastructure.persistence.dataobject.ScrMenuOpDo;
import org.mapstruct.Mapper;

/**
 * 菜单操作 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface MenuOpDoConverter {

    /**
     * 转换为菜单操作实体
     *
     * @param menuOpDo 菜单操作数据对象
     * @return 菜单操作实体
     */
    MenuOpEntity toEntity(ScrMenuOpDo menuOpDo);
}
