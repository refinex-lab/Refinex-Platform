package cn.refinex.system.interfaces.controller;

import cn.refinex.api.user.context.CurrentUserProvider;
import cn.refinex.base.response.MultiResponse;
import cn.refinex.base.response.SingleResponse;
import cn.refinex.system.application.command.AssignRolePermissionsCommand;
import cn.refinex.system.application.command.AssignRoleUsersCommand;
import cn.refinex.system.application.command.CreateRoleCommand;
import cn.refinex.system.application.command.QueryRoleListCommand;
import cn.refinex.system.application.command.UpdateRoleCommand;
import cn.refinex.system.application.dto.RoleBindingDTO;
import cn.refinex.system.application.dto.RoleDTO;
import cn.refinex.system.application.service.SystemApplicationService;
import cn.refinex.system.interfaces.assembler.SystemApiAssembler;
import cn.refinex.system.interfaces.dto.AssignRolePermissionsRequest;
import cn.refinex.system.interfaces.dto.AssignRoleUsersRequest;
import cn.refinex.system.interfaces.dto.RoleCreateRequest;
import cn.refinex.system.interfaces.dto.RoleListQuery;
import cn.refinex.system.interfaces.dto.RoleUpdateRequest;
import cn.refinex.system.interfaces.vo.RoleBindingVO;
import cn.refinex.system.interfaces.vo.RoleVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理接口
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final SystemApplicationService systemApplicationService;
    private final SystemApiAssembler systemApiAssembler;
    private final CurrentUserProvider currentUserProvider;

    /**
     * 查询角色列表
     *
     * @param query 查询条件
     * @return 角色列表
     */
    @GetMapping
    public MultiResponse<RoleVO> listRoles(@Valid RoleListQuery query) {
        QueryRoleListCommand command = systemApiAssembler.toQueryRoleListCommand(query);
        List<RoleDTO> roles = systemApplicationService.listRoles(command);
        return MultiResponse.of(systemApiAssembler.toRoleVoList(roles));
    }

    /**
     * 查询角色详情
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    @GetMapping("/{roleId}")
    public SingleResponse<RoleVO> getRole(@PathVariable @Positive(message = "角色ID必须大于0") Long roleId) {
        RoleDTO role = systemApplicationService.getRole(roleId);
        return SingleResponse.of(systemApiAssembler.toRoleVo(role));
    }

    /**
     * 创建角色
     *
     * @param request 创建请求
     * @return 角色详情
     */
    @PostMapping
    public SingleResponse<RoleVO> createRole(@Valid @RequestBody RoleCreateRequest request) {
        CreateRoleCommand command = systemApiAssembler.toCreateRoleCommand(request);
        RoleDTO created = systemApplicationService.createRole(command);
        return SingleResponse.of(systemApiAssembler.toRoleVo(created));
    }

    /**
     * 更新角色
     *
     * @param roleId  角色ID
     * @param request 更新请求
     * @return 角色详情
     */
    @PutMapping("/{roleId}")
    public SingleResponse<RoleVO> updateRole(@PathVariable @Positive(message = "角色ID必须大于0") Long roleId,
                                             @Valid @RequestBody RoleUpdateRequest request) {
        UpdateRoleCommand command = systemApiAssembler.toUpdateRoleCommand(request);
        command.setRoleId(roleId);
        RoleDTO updated = systemApplicationService.updateRole(command);
        return SingleResponse.of(systemApiAssembler.toRoleVo(updated));
    }

    /**
     * 查询角色授权信息
     *
     * @param roleId 角色ID
     * @return 授权信息
     */
    @GetMapping("/{roleId}/bindings")
    public SingleResponse<RoleBindingVO> getRoleBindings(@PathVariable @Positive(message = "角色ID必须大于0") Long roleId) {
        RoleBindingDTO binding = systemApplicationService.getRoleBindings(roleId);
        return SingleResponse.of(systemApiAssembler.toRoleBindingVo(binding));
    }

    /**
     * 角色授权用户
     *
     * @param roleId  角色ID
     * @param request 授权请求
     * @return 操作结果
     */
    @PutMapping("/{roleId}/users")
    public SingleResponse<Void> assignRoleUsers(@PathVariable @Positive(message = "角色ID必须大于0") Long roleId,
                                                 @Valid @RequestBody AssignRoleUsersRequest request) {
        AssignRoleUsersCommand command = systemApiAssembler.toAssignRoleUsersCommand(request);
        command.setRoleId(roleId);
        command.setOperatorUserId(currentUserProvider.getCurrentUserId());
        systemApplicationService.assignRoleUsers(command);
        return SingleResponse.of(null);
    }

    /**
     * 角色授权权限
     *
     * @param roleId  角色ID
     * @param request 授权请求
     * @return 操作结果
     */
    @PutMapping("/{roleId}/permissions")
    public SingleResponse<Void> assignRolePermissions(@PathVariable @Positive(message = "角色ID必须大于0") Long roleId,
                                                       @Valid @RequestBody AssignRolePermissionsRequest request) {
        AssignRolePermissionsCommand command = systemApiAssembler.toAssignRolePermissionsCommand(request);
        command.setRoleId(roleId);
        command.setOperatorUserId(currentUserProvider.getCurrentUserId());
        systemApplicationService.assignRolePermissions(command);
        return SingleResponse.of(null);
    }
}
