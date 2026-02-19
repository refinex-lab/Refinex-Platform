package cn.refinex.system.interfaces.controller;

import cn.refinex.api.user.model.dto.*;
import cn.refinex.base.response.PageResponse;
import cn.refinex.system.application.service.UserManageApplicationService;
import cn.refinex.system.interfaces.assembler.UserManageApiAssembler;
import cn.refinex.system.interfaces.dto.*;
import cn.refinex.system.interfaces.vo.SystemUserIdentityVO;
import cn.refinex.system.interfaces.vo.SystemUserVO;
import cn.refinex.web.vo.PageResult;
import cn.refinex.web.vo.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统用户管理接口
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/system-users")
@RequiredArgsConstructor
public class UserManageController {

    private final UserManageApplicationService userManageApplicationService;
    private final UserManageApiAssembler userManageApiAssembler;

    /**
     * 查询用户列表
     *
     * @param query 查询参数
     * @return 用户列表
     */
    @GetMapping
    public PageResult<SystemUserVO> listUsers(@Valid SystemUserListQuery query) {
        UserManageListQuery command = userManageApiAssembler.toUserManageListQuery(query);
        PageResponse<UserManageDTO> users = userManageApplicationService.listUsers(command);
        return PageResult.success(
                userManageApiAssembler.toSystemUserVoList(users.getData()),
                users.getTotal(),
                users.getCurrentPage(),
                users.getPageSize()
        );
    }

    /**
     * 查询用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/{userId}")
    public Result<SystemUserVO> getUser(@PathVariable @Positive(message = "用户ID必须大于0") Long userId) {
        UserManageDTO dto = userManageApplicationService.getUser(userId);
        return Result.success(userManageApiAssembler.toSystemUserVo(dto));
    }

    /**
     * 创建用户
     *
     * @param request 创建请求
     * @return 用户详情
     */
    @PostMapping
    public Result<SystemUserVO> createUser(@Valid @RequestBody SystemUserCreateRequest request) {
        UserManageCreateCommand command = userManageApiAssembler.toUserManageCreateCommand(request);
        UserManageDTO dto = userManageApplicationService.createUser(command);
        return Result.success(userManageApiAssembler.toSystemUserVo(dto));
    }

    /**
     * 更新用户
     *
     * @param userId  用户ID
     * @param request 更新请求
     * @return 用户详情
     */
    @PutMapping("/{userId}")
    public Result<SystemUserVO> updateUser(@PathVariable @Positive(message = "用户ID必须大于0") Long userId,
                                           @Valid @RequestBody SystemUserUpdateRequest request) {
        UserManageUpdateCommand command = userManageApiAssembler.toUserManageUpdateCommand(request);
        UserManageDTO dto = userManageApplicationService.updateUser(userId, command);
        return Result.success(userManageApiAssembler.toSystemUserVo(dto));
    }

    /**
     * 查询用户身份列表
     *
     * @param userId 用户ID
     * @return 用户身份列表
     */
    @GetMapping("/{userId}/identities")
    public PageResult<SystemUserIdentityVO> listIdentities(@PathVariable @Positive(message = "用户ID必须大于0") Long userId,
                                                           @Valid SystemUserIdentityListQuery query) {
        PageResponse<UserIdentityManageDTO> identities = userManageApplicationService.listIdentities(
                userId,
                query.getCurrentPage(),
                query.getPageSize()
        );
        return PageResult.success(
                userManageApiAssembler.toSystemUserIdentityVoList(identities.getData()),
                identities.getTotal(),
                identities.getCurrentPage(),
                identities.getPageSize()
        );
    }

    /**
     * 创建用户身份
     *
     * @param userId  用户ID
     * @param request 创建请求
     * @return 用户身份
     */
    @PostMapping("/{userId}/identities")
    public Result<SystemUserIdentityVO> createIdentity(@PathVariable @Positive(message = "用户ID必须大于0") Long userId,
                                                       @Valid @RequestBody SystemUserIdentityCreateRequest request) {
        UserIdentityManageCreateCommand command = userManageApiAssembler.toUserIdentityManageCreateCommand(request);
        UserIdentityManageDTO dto = userManageApplicationService.createIdentity(userId, command);
        return Result.success(userManageApiAssembler.toSystemUserIdentityVo(dto));
    }

    /**
     * 更新用户身份
     *
     * @param identityId 身份ID
     * @param request    更新请求
     * @return 用户身份
     */
    @PutMapping("/identities/{identityId}")
    public Result<SystemUserIdentityVO> updateIdentity(@PathVariable @Positive(message = "身份ID必须大于0") Long identityId,
                                                       @Valid @RequestBody SystemUserIdentityUpdateRequest request) {
        UserIdentityManageUpdateCommand command = userManageApiAssembler.toUserIdentityManageUpdateCommand(request);
        UserIdentityManageDTO dto = userManageApplicationService.updateIdentity(identityId, command);
        return Result.success(userManageApiAssembler.toSystemUserIdentityVo(dto));
    }

    /**
     * 删除用户身份
     *
     * @param identityId 身份ID
     * @return 操作结果
     */
    @DeleteMapping("/identities/{identityId}")
    public Result<Void> deleteIdentity(@PathVariable @Positive(message = "身份ID必须大于0") Long identityId) {
        userManageApplicationService.deleteIdentity(identityId);
        return Result.success();
    }
}
