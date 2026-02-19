package cn.refinex.system.infrastructure.client.user;

import cn.refinex.api.user.model.dto.*;
import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.MultiResponse;
import cn.refinex.base.response.SingleResponse;
import cn.refinex.system.domain.error.SystemErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * 用户管理远程网关
 *
 * @author refinex
 */
@Component
@RequiredArgsConstructor
public class UserManageRemoteGateway {

    private final UserManageHttpClient userManageHttpClient;

    /**
     * 用户管理列表
     *
     * @param query 查询条件
     * @return 用户列表
     */
    public List<UserManageDTO> listUsers(UserManageListQuery query) {
        return invokeList(() -> userManageHttpClient.listUsers(query));
    }

    /**
     * 用户管理详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    public UserManageDTO getUser(Long userId) {
        return invokeSingle(() -> userManageHttpClient.getUser(userId));
    }

    /**
     * 创建用户
     *
     * @param command 创建命令
     * @return 用户详情
     */
    public UserManageDTO createUser(UserManageCreateCommand command) {
        return invokeSingle(() -> userManageHttpClient.createUser(command));
    }

    /**
     * 更新用户
     *
     * @param userId  用户ID
     * @param command 更新命令
     * @return 用户详情
     */
    public UserManageDTO updateUser(Long userId, UserManageUpdateCommand command) {
        return invokeSingle(() -> userManageHttpClient.updateUser(userId, command));
    }

    /**
     * 查询身份列表
     *
     * @param userId 用户ID
     * @return 身份列表
     */
    public List<UserIdentityManageDTO> listIdentities(Long userId) {
        return invokeList(() -> userManageHttpClient.listIdentities(userId));
    }

    /**
     * 创建身份
     *
     * @param userId  用户ID
     * @param command 创建命令
     * @return 身份
     */
    public UserIdentityManageDTO createIdentity(Long userId, UserIdentityManageCreateCommand command) {
        return invokeSingle(() -> userManageHttpClient.createIdentity(userId, command));
    }

    /**
     * 更新身份
     *
     * @param identityId 身份ID
     * @param command    更新命令
     * @return 身份
     */
    public UserIdentityManageDTO updateIdentity(Long identityId, UserIdentityManageUpdateCommand command) {
        return invokeSingle(() -> userManageHttpClient.updateIdentity(identityId, command));
    }

    /**
     * 删除身份
     *
     * @param identityId 身份ID
     */
    public void deleteIdentity(Long identityId) {
        invokeSingle(() -> userManageHttpClient.deleteIdentity(identityId));
    }

    /**
     * 调用用户服务
     *
     * @param supplier 调用方法
     * @param <T>      返回结果类型
     * @return 调用结果
     */
    private <T> T invokeSingle(Supplier<SingleResponse<T>> supplier) {
        try {
            SingleResponse<T> response = supplier.get();
            if (response == null) {
                throw new BizException(SystemErrorCode.INVALID_PARAM);
            }
            if (Boolean.TRUE.equals(response.getSuccess())) {
                return response.getData();
            }
            throw new BizException(response.getResponseMessage(), SystemErrorCode.INVALID_PARAM);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException("调用用户服务失败", ex, SystemErrorCode.INVALID_PARAM);
        }
    }

    /**
     * 调用用户服务
     *
     * @param supplier 调用方法
     * @param <T>      返回结果类型
     * @return 调用结果
     */
    private <T> List<T> invokeList(Supplier<MultiResponse<T>> supplier) {
        try {
            MultiResponse<T> response = supplier.get();
            if (response == null) {
                throw new BizException(SystemErrorCode.INVALID_PARAM);
            }
            if (Boolean.TRUE.equals(response.getSuccess())) {
                return response.getData() == null ? Collections.emptyList() : response.getData();
            }
            throw new BizException(response.getResponseMessage(), SystemErrorCode.INVALID_PARAM);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException("调用用户服务失败", ex, SystemErrorCode.INVALID_PARAM);
        }
    }
}
