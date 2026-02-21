package cn.refinex.system.infrastructure.converter;

import cn.refinex.system.domain.model.entity.OpEntity;
import cn.refinex.system.infrastructure.persistence.dataobject.DefOpDo;
import org.mapstruct.Mapper;

/**
 * 操作定义 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface OpDoConverter {

    /**
     * 转换为操作定义实体
     *
     * @param opDo 操作定义数据对象
     * @return 操作定义实体
     */
    OpEntity toEntity(DefOpDo opDo);
}
