package cn.refinex.user.application.assembler;

import cn.refinex.api.user.enums.UserStatus;
import cn.refinex.api.user.enums.UserType;
import cn.refinex.user.application.dto.AuthSubjectDTO;
import cn.refinex.user.application.dto.UserInfoDTO;
import cn.refinex.user.domain.model.entity.UserAuthSubject;
import cn.refinex.user.domain.model.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * 应用层领域转换器
 *
 * @author refinex
 */
@Mapper(componentModel = "spring")
public interface UserDomainAssembler {

    /**
     * 将用户认证主题转换为用户认证主题DTO
     *
     * @param subject 用户认证主题
     * @return 用户认证主题DTO
     */
    @Mapping(target = "userId", source = "subject.user.id")
    @Mapping(target = "userCode", source = "subject.user.userCode")
    @Mapping(target = "username", source = "subject.user.username")
    @Mapping(target = "displayName", source = "subject.user.displayName")
    @Mapping(target = "nickname", source = "subject.user.nickname")
    @Mapping(target = "avatarUrl", source = "subject.user.avatarUrl")
    @Mapping(target = "userType", source = "subject.user.userType")
    @Mapping(target = "userStatus", source = "subject.user.status")
    @Mapping(target = "primaryEstabId", source = "subject.user.primaryEstabId")
    @Mapping(target = "loginFailCount", source = "subject.user.loginFailCount")
    @Mapping(target = "lockUntil", source = "subject.user.lockUntil")
    @Mapping(target = "identityId", source = "subject.identity.id")
    @Mapping(target = "identityStatus", source = "subject.identity.status")
    @Mapping(target = "credential", source = "subject.identity.credential")
    @Mapping(target = "teamId", source = "subject.teamId")
    @Mapping(target = "estabAdmin", source = "subject.estabAdmin")
    AuthSubjectDTO toAuthSubjectDto(UserAuthSubject subject);

    /**
     * 将用户转换为用户信息DTO
     *
     * @param user 用户
     * @param teamId 用户所属团队ID
     * @param estabAdmin 用户是否是机构管理员
     * @return 用户信息DTO
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "status", source = "user.status", qualifiedByName = "toUserStatus")
    @Mapping(target = "userType", source = "user.userType", qualifiedByName = "toUserType")
    @Mapping(target = "phoneVerified", expression = "java(user.getPhoneVerified() != null && user.getPhoneVerified() == 1)")
    @Mapping(target = "emailVerified", expression = "java(user.getEmailVerified() != null && user.getEmailVerified() == 1)")
    @Mapping(target = "registerTime", source = "user.gmtCreate")
    @Mapping(target = "primaryTeamId", source = "teamId")
    @Mapping(target = "estabAdmin", source = "estabAdmin")
    UserInfoDTO toUserInfoDto(UserEntity user, Long teamId, Boolean estabAdmin);

    /**
     * 将用户状态转换为枚举
     *
     * @param status 用户状态
     * @return 用户状态枚举
     */
    @Named("toUserStatus")
    default UserStatus toUserStatus(Integer status) {
        return UserStatus.of(status);
    }

    /**
     * 将用户类型转换为枚举
     *
     * @param userType 用户类型
     * @return 用户类型枚举
     */
    @Named("toUserType")
    default UserType toUserType(Integer userType) {
        return UserType.of(userType);
    }
}
