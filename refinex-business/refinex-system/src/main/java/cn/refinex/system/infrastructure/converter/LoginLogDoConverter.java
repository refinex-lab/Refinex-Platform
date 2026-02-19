package cn.refinex.system.infrastructure.converter;

import cn.refinex.system.domain.model.entity.LoginLogEntity;
import cn.refinex.system.infrastructure.persistence.dataobject.LogLoginDo;
import org.mapstruct.Mapper;

/**
 * 登录日志 DO 转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface LoginLogDoConverter {

    /**
     * 转换为登录日志实体
     *
     * @param loginLogDo 登录日志数据对象
     * @return 登录日志实体
     */
    LoginLogEntity toEntity(LogLoginDo loginLogDo);
}
