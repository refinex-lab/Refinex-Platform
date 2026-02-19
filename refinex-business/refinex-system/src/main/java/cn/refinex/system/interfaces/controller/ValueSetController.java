package cn.refinex.system.interfaces.controller;

import cn.refinex.base.response.PageResponse;
import cn.refinex.system.application.command.*;
import cn.refinex.system.application.dto.ValueDTO;
import cn.refinex.system.application.dto.ValueSetDTO;
import cn.refinex.system.application.service.ValueSetApplicationService;
import cn.refinex.system.interfaces.assembler.SystemApiAssembler;
import cn.refinex.system.interfaces.dto.*;
import cn.refinex.system.interfaces.vo.ValueSetVO;
import cn.refinex.system.interfaces.vo.ValueVO;
import cn.refinex.web.vo.PageResult;
import cn.refinex.web.vo.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 值集管理接口
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/valuesets")
@RequiredArgsConstructor
public class ValueSetController {

    private final ValueSetApplicationService valueSetApplicationService;
    private final SystemApiAssembler systemApiAssembler;

    /**
     * 查询值集列表
     *
     * @param query 查询条件
     * @return 值集列表
     */
    @GetMapping
    public PageResult<ValueSetVO> listValueSets(@Valid ValueSetListQuery query) {
        QueryValueSetListCommand command = systemApiAssembler.toQueryValueSetListCommand(query);
        PageResponse<ValueSetDTO> list = valueSetApplicationService.listValueSets(command);
        return PageResult.success(
                systemApiAssembler.toValueSetVoList(list.getData()),
                list.getTotal(),
                list.getCurrentPage(),
                list.getPageSize()
        );
    }

    /**
     * 查询值集详情
     *
     * @param valueSetId 值集ID
     * @return 值集详情
     */
    @GetMapping("/{valueSetId}")
    public Result<ValueSetVO> getValueSet(@PathVariable @Positive(message = "值集ID必须大于0") Long valueSetId) {
        ValueSetDTO dto = valueSetApplicationService.getValueSet(valueSetId);
        return Result.success(systemApiAssembler.toValueSetVo(dto));
    }

    /**
     * 创建值集
     *
     * @param request 创建请求
     * @return 值集详情
     */
    @PostMapping
    public Result<ValueSetVO> createValueSet(@Valid @RequestBody ValueSetCreateRequest request) {
        CreateValueSetCommand command = systemApiAssembler.toCreateValueSetCommand(request);
        ValueSetDTO dto = valueSetApplicationService.createValueSet(command);
        return Result.success(systemApiAssembler.toValueSetVo(dto));
    }

    /**
     * 更新值集
     *
     * @param valueSetId 值集ID
     * @param request    更新请求
     * @return 值集详情
     */
    @PutMapping("/{valueSetId}")
    public Result<ValueSetVO> updateValueSet(@PathVariable @Positive(message = "值集ID必须大于0") Long valueSetId,
                                             @Valid @RequestBody ValueSetUpdateRequest request) {
        UpdateValueSetCommand command = systemApiAssembler.toUpdateValueSetCommand(request);
        command.setValueSetId(valueSetId);
        ValueSetDTO dto = valueSetApplicationService.updateValueSet(command);
        return Result.success(systemApiAssembler.toValueSetVo(dto));
    }

    /**
     * 删除值集
     *
     * @param valueSetId 值集ID
     * @return 操作结果
     */
    @DeleteMapping("/{valueSetId}")
    public Result<Void> deleteValueSet(@PathVariable @Positive(message = "值集ID必须大于0") Long valueSetId) {
        valueSetApplicationService.deleteValueSet(valueSetId);
        return Result.success();
    }

    /**
     * 查询值集明细列表
     *
     * @param query 查询条件
     * @return 值集明细列表
     */
    @GetMapping("/values")
    public PageResult<ValueVO> listValues(@Valid ValueListQuery query) {
        QueryValueListCommand command = systemApiAssembler.toQueryValueListCommand(query);
        PageResponse<ValueDTO> list = valueSetApplicationService.listValues(command);
        return PageResult.success(
                systemApiAssembler.toValueVoList(list.getData()),
                list.getTotal(),
                list.getCurrentPage(),
                list.getPageSize()
        );
    }

    /**
     * 查询值集明细详情
     *
     * @param valueId 值ID
     * @return 值集明细详情
     */
    @GetMapping("/values/{valueId}")
    public Result<ValueVO> getValue(@PathVariable @Positive(message = "值ID必须大于0") Long valueId) {
        ValueDTO dto = valueSetApplicationService.getValue(valueId);
        return Result.success(systemApiAssembler.toValueVo(dto));
    }

    /**
     * 创建值集明细
     *
     * @param setCode 值集编码
     * @param request 创建请求
     * @return 值集明细详情
     */
    @PostMapping("/{setCode}/values")
    public Result<ValueVO> createValue(@PathVariable String setCode,
                                       @Valid @RequestBody ValueCreateRequest request) {
        CreateValueCommand command = systemApiAssembler.toCreateValueCommand(request);
        command.setSetCode(setCode);
        ValueDTO dto = valueSetApplicationService.createValue(command);
        return Result.success(systemApiAssembler.toValueVo(dto));
    }

    /**
     * 更新值集明细
     *
     * @param valueId 值ID
     * @param request 更新请求
     * @return 值集明细详情
     */
    @PutMapping("/values/{valueId}")
    public Result<ValueVO> updateValue(@PathVariable @Positive(message = "值ID必须大于0") Long valueId,
                                       @Valid @RequestBody ValueUpdateRequest request) {
        UpdateValueCommand command = systemApiAssembler.toUpdateValueCommand(request);
        command.setValueId(valueId);
        ValueDTO dto = valueSetApplicationService.updateValue(command);
        return Result.success(systemApiAssembler.toValueVo(dto));
    }

    /**
     * 删除值集明细
     *
     * @param valueId 值ID
     * @return 操作结果
     */
    @DeleteMapping("/values/{valueId}")
    public Result<Void> deleteValue(@PathVariable @Positive(message = "值ID必须大于0") Long valueId) {
        valueSetApplicationService.deleteValue(valueId);
        return Result.success();
    }
}
