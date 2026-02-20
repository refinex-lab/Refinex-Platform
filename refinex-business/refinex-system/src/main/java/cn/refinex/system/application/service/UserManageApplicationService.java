package cn.refinex.system.application.service;

import cn.refinex.api.user.model.dto.*;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.PageResponse;
import cn.refinex.base.utils.PageUtils;
import cn.refinex.satoken.helper.LoginUserHelper;
import cn.refinex.system.domain.error.SystemErrorCode;
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

    private static final String BUILTIN_SUPER_ADMIN_USER_CODE = "U_REFINEX_SUPER_ADMIN";

    private final UserManageRemoteGateway userManageRemoteGateway;

    /**
     * 查询用户列表
     *
     * @param query 查询条件
     * @return 用户列表
     */
    public PageResponse<UserManageDTO> listUsers(UserManageListQuery query) {
        if (query == null) {
            query = new UserManageListQuery();
        }
        query.setCurrentPage(PageUtils.normalizeCurrentPage(query.getCurrentPage()));
        query.setPageSize(PageUtils.normalizePageSize(
                query.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE,
                PageUtils.DEFAULT_MAX_PAGE_SIZE
        ));
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
     * 删除用户
     *
     * @param userId 用户ID
     */
    public void deleteUser(Long userId) {
        validateDeleteTarget(userManageRemoteGateway.getUser(userId));
        userManageRemoteGateway.deleteUser(userId);
    }

    /**
     * 批量删除用户
     *
     * @param command 批量删除命令
     */
    public void deleteUsers(UserManageBatchDeleteCommand command) {
        if (command == null || command.getUserIds() == null || command.getUserIds().isEmpty()) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }
        for (Long userId : command.getUserIds()) {
            validateDeleteTarget(userManageRemoteGateway.getUser(userId));
        }
        userManageRemoteGateway.batchDeleteUsers(command);
    }

    /**
     * 校验目标用户是否允许删除
     *
     * @param user 目标用户
     */
    private void validateDeleteTarget(UserManageDTO user) {
        if (user == null || user.getUserId() == null) {
            throw new BizException(SystemErrorCode.INVALID_PARAM);
        }

        if (BUILTIN_SUPER_ADMIN_USER_CODE.equalsIgnoreCase(user.getUserCode())) {
            throw new BizException("内置超级管理员账号不允许删除", SystemErrorCode.INVALID_PARAM);
        }

        Long currentUserId = LoginUserHelper.getUserId();
        if (currentUserId != null && currentUserId.equals(user.getUserId())) {
            throw new BizException("不允许删除当前登录用户", SystemErrorCode.INVALID_PARAM);
        }
    }

    /**
     * 查询用户身份列表
     *
     * @param userId 用户ID
     * @return 用户身份列表
     */
    public PageResponse<UserIdentityManageDTO> listIdentities(Long userId, int currentPage, int pageSize) {
        List<UserIdentityManageDTO> identities = userManageRemoteGateway.listIdentities(userId);
        return PageUtils.slice(
                identities,
                PageUtils.normalizeCurrentPage(currentPage),
                PageUtils.normalizePageSize(pageSize, PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE)
        );
    }

    /**
     * 查询用户所属企业列表
     *
     * @param userId 用户ID
     * @return 所属企业列表
     */
    public List<UserManageEstabDTO> listUserEstabs(Long userId) {
        return userManageRemoteGateway.listUserEstabs(userId);
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
