package cn.refinex.system.infrastructure.client.user;

import cn.refinex.api.user.model.dto.*;
import cn.refinex.base.response.MultiResponse;
import cn.refinex.base.response.PageResponse;
import cn.refinex.base.response.SingleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理 HTTP 客户端
 *
 * @author refinex
 */
@FeignClient(name = "refinex-user", path = "/internal/users")
public interface UserManageHttpClient {

    /**
     * 用户管理列表
     *
     * @param query 查询条件
     * @return 用户列表
     */
    @PostMapping("/manage/list")
    PageResponse<UserManageDTO> listUsers(@RequestBody UserManageListQuery query);

    /**
     * 用户管理详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/manage/{userId}")
    SingleResponse<UserManageDTO> getUser(@PathVariable("userId") Long userId);

    /**
     * 创建用户
     *
     * @param command 创建命令
     * @return 用户详情
     */
    @PostMapping("/manage")
    SingleResponse<UserManageDTO> createUser(@RequestBody UserManageCreateCommand command);

    /**
     * 更新用户
     *
     * @param userId  用户ID
     * @param command 更新命令
     * @return 用户详情
     */
    @PutMapping("/manage/{userId}")
    SingleResponse<UserManageDTO> updateUser(@PathVariable("userId") Long userId, @RequestBody UserManageUpdateCommand command);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/manage/{userId}")
    SingleResponse<Void> deleteUser(@PathVariable("userId") Long userId);

    /**
     * 批量删除用户
     *
     * @param command 批量删除命令
     * @return 操作结果
     */
    @PostMapping("/manage/batch-delete")
    SingleResponse<Void> batchDeleteUsers(@RequestBody UserManageBatchDeleteCommand command);

    /**
     * 查询身份列表
     *
     * @param userId 用户ID
     * @return 身份列表
     */
    @GetMapping("/manage/{userId}/identities")
    MultiResponse<UserIdentityManageDTO> listIdentities(@PathVariable("userId") Long userId);

    /**
     * 查询用户所属企业列表
     *
     * @param userId 用户ID
     * @return 所属企业列表
     */
    @GetMapping("/manage/{userId}/estabs")
    MultiResponse<UserManageEstabDTO> listUserEstabs(@PathVariable("userId") Long userId);

    /**
     * 创建身份
     *
     * @param userId  用户ID
     * @param command 创建命令
     * @return 身份
     */
    @PostMapping("/manage/{userId}/identities")
    SingleResponse<UserIdentityManageDTO> createIdentity(@PathVariable("userId") Long userId,
                                                         @RequestBody UserIdentityManageCreateCommand command);

    /**
     * 更新身份
     *
     * @param identityId 身份ID
     * @param command    更新命令
     * @return 身份
     */
    @PutMapping("/manage/identities/{identityId}")
    SingleResponse<UserIdentityManageDTO> updateIdentity(@PathVariable("identityId") Long identityId,
                                                         @RequestBody UserIdentityManageUpdateCommand command);

    /**
     * 删除身份
     *
     * @param identityId 身份ID
     * @return 操作结果
     */
    @DeleteMapping("/manage/identities/{identityId}")
    SingleResponse<Void> deleteIdentity(@PathVariable("identityId") Long identityId);
}
