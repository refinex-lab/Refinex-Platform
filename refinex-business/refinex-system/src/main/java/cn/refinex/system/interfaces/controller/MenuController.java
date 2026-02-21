package cn.refinex.system.interfaces.controller;

import cn.refinex.base.response.PageResponse;
import cn.refinex.system.application.command.CreateMenuCommand;
import cn.refinex.system.application.command.CreateMenuOpCommand;
import cn.refinex.system.application.command.QueryMenuTreeCommand;
import cn.refinex.system.application.command.UpdateMenuCommand;
import cn.refinex.system.application.command.UpdateMenuOpCommand;
import cn.refinex.system.application.dto.MenuDTO;
import cn.refinex.system.application.dto.MenuOpManageDTO;
import cn.refinex.system.application.dto.MenuTreeNodeDTO;
import cn.refinex.system.application.dto.OpDTO;
import cn.refinex.system.application.service.SystemApplicationService;
import cn.refinex.system.interfaces.assembler.SystemApiAssembler;
import cn.refinex.system.interfaces.dto.MenuCreateRequest;
import cn.refinex.system.interfaces.dto.MenuOpListQuery;
import cn.refinex.system.interfaces.dto.MenuOpCreateRequest;
import cn.refinex.system.interfaces.dto.MenuOpUpdateRequest;
import cn.refinex.system.interfaces.dto.MenuReorderItem;
import cn.refinex.system.interfaces.dto.MenuTreeQuery;
import cn.refinex.system.interfaces.dto.MenuUpdateRequest;
import cn.refinex.system.interfaces.vo.MenuOpManageVO;
import cn.refinex.system.interfaces.vo.MenuVO;
import cn.refinex.system.interfaces.vo.MenuTreeNodeVO;
import cn.refinex.system.interfaces.vo.OpVO;
import cn.refinex.web.vo.PageResult;
import cn.refinex.web.vo.Result;
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
    public Result<List<MenuTreeNodeVO>> tree(@Valid MenuTreeQuery query) {
        QueryMenuTreeCommand command = systemApiAssembler.toQueryMenuTreeCommand(query);
        List<MenuTreeNodeDTO> tree = systemApplicationService.getMenuTree(command);
        return Result.success(systemApiAssembler.toMenuTreeNodeVoList(tree));
    }

    /**
     * 查询菜单详情
     *
     * @param menuId 菜单ID
     * @return 菜单详情
     */
    @GetMapping("/{menuId}")
    public Result<MenuVO> getMenu(@PathVariable @Positive(message = "菜单ID必须大于0") Long menuId) {
        MenuDTO menu = systemApplicationService.getMenu(menuId);
        return Result.success(systemApiAssembler.toMenuVo(menu));
    }

    /**
     * 创建菜单
     *
     * @param request 创建请求
     * @return 菜单详情
     */
    @PostMapping
    public Result<MenuVO> createMenu(@Valid @RequestBody MenuCreateRequest request) {
        CreateMenuCommand command = systemApiAssembler.toCreateMenuCommand(request);
        MenuDTO created = systemApplicationService.createMenu(command);
        return Result.success(systemApiAssembler.toMenuVo(created));
    }

    /**
     * 更新菜单
     *
     * @param menuId  菜单ID
     * @param request 更新请求
     * @return 菜单详情
     */
    @PutMapping("/{menuId}")
    public Result<MenuVO> updateMenu(@PathVariable @Positive(message = "菜单ID必须大于0") Long menuId,
                                     @Valid @RequestBody MenuUpdateRequest request) {
        UpdateMenuCommand command = systemApiAssembler.toUpdateMenuCommand(request);
        command.setMenuId(menuId);
        MenuDTO updated = systemApplicationService.updateMenu(command);
        return Result.success(systemApiAssembler.toMenuVo(updated));
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     * @return 操作结果
     */
    @DeleteMapping("/{menuId}")
    public Result<Void> deleteMenu(@PathVariable @Positive(message = "菜单ID必须大于0") Long menuId) {
        systemApplicationService.deleteMenu(menuId);
        return Result.success();
    }

    /**
     * 查询菜单操作列表
     *
     * @param menuId 菜单ID
     * @return 菜单操作列表
     */
    @GetMapping("/{menuId}/ops")
    public PageResult<MenuOpManageVO> listMenuOps(@PathVariable @Positive(message = "菜单ID必须大于0") Long menuId,
                                                  @Valid MenuOpListQuery query) {
        PageResponse<MenuOpManageDTO> ops = systemApplicationService.listMenuOps(
                menuId,
                query.getCurrentPage(),
                query.getPageSize()
        );
        return PageResult.success(
                systemApiAssembler.toMenuOpManageVoList(ops.getData()),
                ops.getTotal(),
                ops.getCurrentPage(),
                ops.getPageSize()
        );
    }

    /**
     * 查询菜单操作详情
     *
     * @param menuOpId 菜单操作ID
     * @return 菜单操作详情
     */
    @GetMapping("/ops/{menuOpId}")
    public Result<MenuOpManageVO> getMenuOp(@PathVariable @Positive(message = "菜单操作ID必须大于0") Long menuOpId) {
        MenuOpManageDTO menuOp = systemApplicationService.getMenuOp(menuOpId);
        return Result.success(systemApiAssembler.toMenuOpManageVo(menuOp));
    }

    /**
     * 创建菜单操作
     *
     * @param menuId  菜单ID
     * @param request 创建请求
     * @return 菜单操作详情
     */
    @PostMapping("/{menuId}/ops")
    public Result<MenuOpManageVO> createMenuOp(@PathVariable @Positive(message = "菜单ID必须大于0") Long menuId,
                                               @Valid @RequestBody MenuOpCreateRequest request) {
        CreateMenuOpCommand command = systemApiAssembler.toCreateMenuOpCommand(request);
        command.setMenuId(menuId);
        MenuOpManageDTO created = systemApplicationService.createMenuOp(command);
        return Result.success(systemApiAssembler.toMenuOpManageVo(created));
    }

    /**
     * 更新菜单操作
     *
     * @param menuOpId 菜单操作ID
     * @param request  更新请求
     * @return 菜单操作详情
     */
    @PutMapping("/ops/{menuOpId}")
    public Result<MenuOpManageVO> updateMenuOp(@PathVariable @Positive(message = "菜单操作ID必须大于0") Long menuOpId,
                                               @Valid @RequestBody MenuOpUpdateRequest request) {
        UpdateMenuOpCommand command = systemApiAssembler.toUpdateMenuOpCommand(request);
        command.setMenuOpId(menuOpId);
        MenuOpManageDTO updated = systemApplicationService.updateMenuOp(command);
        return Result.success(systemApiAssembler.toMenuOpManageVo(updated));
    }

    /**
     * 删除菜单操作
     *
     * @param menuOpId 菜单操作ID
     * @return 操作结果
     */
    @DeleteMapping("/ops/{menuOpId}")
    public Result<Void> deleteMenuOp(@PathVariable @Positive(message = "菜单操作ID必须大于0") Long menuOpId) {
        systemApplicationService.deleteMenuOp(menuOpId);
        return Result.success();
    }

    /**
     * 查询启用的操作定义列表
     *
     * @return 操作定义列表
     */
    @GetMapping("/op-definitions")
    public Result<List<OpVO>> listOpDefinitions() {
        List<OpDTO> ops = systemApplicationService.listOps();
        return Result.success(systemApiAssembler.toOpVoList(ops));
    }

    /**
     * 批量调整菜单排序和层级
     *
     * @param items 排序项列表
     * @return 操作结果
     */
    @PutMapping("/reorder")
    public Result<Void> reorderMenus(@Valid @RequestBody List<MenuReorderItem> items) {
        systemApplicationService.reorderMenus(items);
        return Result.success();
    }
}
