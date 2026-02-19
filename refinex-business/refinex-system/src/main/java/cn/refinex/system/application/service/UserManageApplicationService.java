package cn.refinex.system.application.service;

import cn.refinex.api.user.model.dto.*;
import cn.refinex.system.infrastructure.client.user.UserManageRemoteGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统用户管理应用服务
 *
 * @author refinex
 */
@Service
@RequiredArgsConstructor
public class UserManageApplicationService {

    private final UserManageRemoteGateway userManageRemoteGateway;

    /**
     * 查询用户列表
     *
     * @param query 查询条件
     * @return 用户列表
     */
    public List<UserManageDTO> listUsers(UserManageListQuery query) {
        return userManageRemoteGateway.listUsers(query);
    }

    /**
     * 查询用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    public UserManageDTO getUser(Long userId) {
        return userManageRemoteGateway.getUser(userId);
    }

    /**
     * 创建用户
     *
     * @param command 创建命令
     * @return 用户详情
     */
    public UserManageDTO createUser(UserManageCreateCommand command) {
        return userManageRemoteGateway.createUser(command);
    }

    /**
     * 更新用户
     *
     * @param userId  用户ID
     * @param command 更新命令
     * @return 用户详情
     */
    public UserManageDTO updateUser(Long userId, UserManageUpdateCommand command) {
        return userManageRemoteGateway.updateUser(userId, command);
    }

    /**
     * 查询用户身份列表
     *
     * @param userId 用户ID
     * @return 用户身份列表
     */
    public List<UserIdentityManageDTO> listIdentities(Long userId) {
        return userManageRemoteGateway.listIdentities(userId);
    }

    /**
     * 创建用户身份
     *
     * @param userId  用户ID
     * @param command 创建命令
     * @return 用户身份
     */
    public UserIdentityManageDTO createIdentity(Long userId, UserIdentityManageCreateCommand command) {
        return userManageRemoteGateway.createIdentity(userId, command);
    }

    /**
     * 更新用户身份
     *
     * @param identityId 身份ID
     * @param command    更新命令
     * @return 用户身份
     */
    public UserIdentityManageDTO updateIdentity(Long identityId, UserIdentityManageUpdateCommand command) {
        return userManageRemoteGateway.updateIdentity(identityId, command);
    }

    /**
     * 删除用户身份
     *
     * @param identityId 身份ID
     */
    public void deleteIdentity(Long identityId) {
        userManageRemoteGateway.deleteIdentity(identityId);
    }
}
