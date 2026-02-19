package cn.refinex.system.interfaces.controller;

import cn.refinex.base.response.MultiResponse;
import cn.refinex.base.response.SingleResponse;
import cn.refinex.system.application.command.CreateMenuCommand;
import cn.refinex.system.application.command.CreateMenuOpCommand;
import cn.refinex.system.application.command.QueryMenuTreeCommand;
import cn.refinex.system.application.command.UpdateMenuCommand;
import cn.refinex.system.application.command.UpdateMenuOpCommand;
import cn.refinex.system.application.dto.MenuDTO;
import cn.refinex.system.application.dto.MenuOpManageDTO;
import cn.refinex.system.application.dto.MenuTreeNodeDTO;
import cn.refinex.system.application.service.SystemApplicationService;
import cn.refinex.system.interfaces.assembler.SystemApiAssembler;
import cn.refinex.system.interfaces.dto.MenuCreateRequest;
import cn.refinex.system.interfaces.dto.MenuOpCreateRequest;
import cn.refinex.system.interfaces.dto.MenuOpUpdateRequest;
import cn.refinex.system.interfaces.dto.MenuTreeQuery;
import cn.refinex.system.interfaces.dto.MenuUpdateRequest;
import cn.refinex.system.interfaces.vo.MenuOpManageVO;
import cn.refinex.system.interfaces.vo.MenuVO;
import cn.refinex.system.interfaces.vo.MenuTreeNodeVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单管理接口
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final SystemApplicationService systemApplicationService;
    private final SystemApiAssembler systemApiAssembler;

    /**
     * 根据系统查询菜单树
     *
     * @param query 查询条件
     * @return 菜单树
     */
    @GetMapping("/tree")
    public MultiResponse<MenuTreeNodeVO> tree(@Valid MenuTreeQuery query) {
        QueryMenuTreeCommand command = systemApiAssembler.toQueryMenuTreeCommand(query);
        List<MenuTreeNodeDTO> tree = systemApplicationService.getMenuTree(command);
        return MultiResponse.of(systemApiAssembler.toMenuTreeNodeVoList(tree));
    }

    /**
     * 查询菜单详情
     *
     * @param menuId 菜单ID
     * @return 菜单详情
     */
    @GetMapping("/{menuId}")
    public SingleResponse<MenuVO> getMenu(@PathVariable @Positive(message = "菜单ID必须大于0") Long menuId) {
        MenuDTO menu = systemApplicationService.getMenu(menuId);
        return SingleResponse.of(systemApiAssembler.toMenuVo(menu));
    }

    /**
     * 创建菜单
     *
     * @param request 创建请求
     * @return 菜单详情
     */
    @PostMapping
    public SingleResponse<MenuVO> createMenu(@Valid @RequestBody MenuCreateRequest request) {
        CreateMenuCommand command = systemApiAssembler.toCreateMenuCommand(request);
        MenuDTO created = systemApplicationService.createMenu(command);
        return SingleResponse.of(systemApiAssembler.toMenuVo(created));
    }

    /**
     * 更新菜单
     *
     * @param menuId  菜单ID
     * @param request 更新请求
     * @return 菜单详情
     */
    @PutMapping("/{menuId}")
    public SingleResponse<MenuVO> updateMenu(@PathVariable @Positive(message = "菜单ID必须大于0") Long menuId,
                                             @Valid @RequestBody MenuUpdateRequest request) {
        UpdateMenuCommand command = systemApiAssembler.toUpdateMenuCommand(request);
        command.setMenuId(menuId);
        MenuDTO updated = systemApplicationService.updateMenu(command);
        return SingleResponse.of(systemApiAssembler.toMenuVo(updated));
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     * @return 操作结果
     */
    @DeleteMapping("/{menuId}")
    public SingleResponse<Void> deleteMenu(@PathVariable @Positive(message = "菜单ID必须大于0") Long menuId) {
        systemApplicationService.deleteMenu(menuId);
        return SingleResponse.of(null);
    }

    /**
     * 查询菜单操作列表
     *
     * @param menuId 菜单ID
     * @return 菜单操作列表
     */
    @GetMapping("/{menuId}/ops")
    public MultiResponse<MenuOpManageVO> listMenuOps(@PathVariable @Positive(message = "菜单ID必须大于0") Long menuId) {
        List<MenuOpManageDTO> ops = systemApplicationService.listMenuOps(menuId);
        return MultiResponse.of(systemApiAssembler.toMenuOpManageVoList(ops));
    }

    /**
     * 查询菜单操作详情
     *
     * @param menuOpId 菜单操作ID
     * @return 菜单操作详情
     */
    @GetMapping("/ops/{menuOpId}")
    public SingleResponse<MenuOpManageVO> getMenuOp(@PathVariable @Positive(message = "菜单操作ID必须大于0") Long menuOpId) {
        MenuOpManageDTO menuOp = systemApplicationService.getMenuOp(menuOpId);
        return SingleResponse.of(systemApiAssembler.toMenuOpManageVo(menuOp));
    }

    /**
     * 创建菜单操作
     *
     * @param menuId  菜单ID
     * @param request 创建请求
     * @return 菜单操作详情
     */
    @PostMapping("/{menuId}/ops")
    public SingleResponse<MenuOpManageVO> createMenuOp(@PathVariable @Positive(message = "菜单ID必须大于0") Long menuId,
                                                        @Valid @RequestBody MenuOpCreateRequest request) {
        CreateMenuOpCommand command = systemApiAssembler.toCreateMenuOpCommand(request);
        command.setMenuId(menuId);
        MenuOpManageDTO created = systemApplicationService.createMenuOp(command);
        return SingleResponse.of(systemApiAssembler.toMenuOpManageVo(created));
    }

    /**
     * 更新菜单操作
     *
     * @param menuOpId 菜单操作ID
     * @param request  更新请求
     * @return 菜单操作详情
     */
    @PutMapping("/ops/{menuOpId}")
    public SingleResponse<MenuOpManageVO> updateMenuOp(@PathVariable @Positive(message = "菜单操作ID必须大于0") Long menuOpId,
                                                        @Valid @RequestBody MenuOpUpdateRequest request) {
        UpdateMenuOpCommand command = systemApiAssembler.toUpdateMenuOpCommand(request);
        command.setMenuOpId(menuOpId);
        MenuOpManageDTO updated = systemApplicationService.updateMenuOp(command);
        return SingleResponse.of(systemApiAssembler.toMenuOpManageVo(updated));
    }

    /**
     * 删除菜单操作
     *
     * @param menuOpId 菜单操作ID
     * @return 操作结果
     */
    @DeleteMapping("/ops/{menuOpId}")
    public SingleResponse<Void> deleteMenuOp(@PathVariable @Positive(message = "菜单操作ID必须大于0") Long menuOpId) {
        systemApplicationService.deleteMenuOp(menuOpId);
        return SingleResponse.of(null);
    }
}
