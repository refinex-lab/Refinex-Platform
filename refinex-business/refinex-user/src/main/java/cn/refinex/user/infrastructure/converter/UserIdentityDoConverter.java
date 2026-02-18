package cn.refinex.user.infrastructure.converter;

import cn.refinex.user.domain.model.entity.UserIdentityEntity;
import cn.refinex.user.infrastructure.persistence.dataobject.DefUserIdentityDo;
import org.mapstruct.Mapper;

/**
 * 用户身份转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface UserIdentityDoConverter {

    /**
     * 将数据对象转换为实体
     *
     * @param identityDo 数据对象
     * @return 实体
     */
    UserIdentityEntity toEntity(DefUserIdentityDo identityDo);

    /**
     * 将实体转换为数据对象
     *
     * @param identityEntity 实体
     * @return 数据对象
     */
    DefUserIdentityDo toDo(UserIdentityEntity identityEntity);
}
