package cn.refinex.user.infrastructure.converter;

import cn.refinex.user.domain.model.entity.UserEntity;
import cn.refinex.user.infrastructure.persistence.dataobject.DefUserDo;
import org.mapstruct.Mapper;

/**
 * 用户实体转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface UserDoConverter {

    /**
     * 将数据对象转换为实体
     *
     * @param userDo 数据对象
     * @return 实体
     */
    UserEntity toEntity(DefUserDo userDo);

    /**
     * 将实体转换为数据对象
     *
     * @param userEntity 实体
     * @return 数据对象
     */
    DefUserDo toDo(UserEntity userEntity);
}
