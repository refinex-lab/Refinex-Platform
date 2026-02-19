package cn.refinex.system.infrastructure.client.user;

import cn.refinex.api.user.model.dto.*;
import cn.refinex.base.response.MultiResponse;
import cn.refinex.base.response.SingleResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.*;

/**
 * 用户管理 HTTP 客户端
 *
 * @author refinex
 */
@HttpExchange("/internal/users")
public interface UserManageHttpClient {

    /**
     * 用户管理列表
     *
     * @param query 查询条件
     * @return 用户列表
     */
    @PostExchange("/manage/list")
    MultiResponse<UserManageDTO> listUsers(@RequestBody UserManageListQuery query);

    /**
     * 用户管理详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetExchange("/manage/{userId}")
    SingleResponse<UserManageDTO> getUser(@PathVariable Long userId);

    /**
     * 创建用户
     *
     * @param command 创建命令
     * @return 用户详情
     */
    @PostExchange("/manage")
    SingleResponse<UserManageDTO> createUser(@RequestBody UserManageCreateCommand command);

    /**
     * 更新用户
     *
     * @param userId  用户ID
     * @param command 更新命令
     * @return 用户详情
     */
    @PutExchange("/manage/{userId}")
    SingleResponse<UserManageDTO> updateUser(@PathVariable Long userId, @RequestBody UserManageUpdateCommand command);

    /**
     * 查询身份列表
     *
     * @param userId 用户ID
     * @return 身份列表
     */
    @GetExchange("/manage/{userId}/identities")
    MultiResponse<UserIdentityManageDTO> listIdentities(@PathVariable Long userId);

    /**
     * 创建身份
     *
     * @param userId  用户ID
     * @param command 创建命令
     * @return 身份
     */
    @PostExchange("/manage/{userId}/identities")
    SingleResponse<UserIdentityManageDTO> createIdentity(@PathVariable Long userId,
                                                         @RequestBody UserIdentityManageCreateCommand command);

    /**
     * 更新身份
     *
     * @param identityId 身份ID
     * @param command    更新命令
     * @return 身份
     */
    @PutExchange("/manage/identities/{identityId}")
    SingleResponse<UserIdentityManageDTO> updateIdentity(@PathVariable Long identityId,
                                                         @RequestBody UserIdentityManageUpdateCommand command);

    /**
     * 删除身份
     *
     * @param identityId 身份ID
     * @return 操作结果
     */
    @DeleteExchange("/manage/identities/{identityId}")
    SingleResponse<Void> deleteIdentity(@PathVariable Long identityId);
}
