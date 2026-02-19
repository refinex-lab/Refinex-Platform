package cn.refinex.system.interfaces.controller;

import cn.refinex.base.response.MultiResponse;
import cn.refinex.base.response.SingleResponse;
import cn.refinex.system.application.command.*;
import cn.refinex.system.application.dto.DrsDTO;
import cn.refinex.system.application.dto.DrsInterfaceDTO;
import cn.refinex.system.application.service.DataResourceApplicationService;
import cn.refinex.system.interfaces.assembler.SystemApiAssembler;
import cn.refinex.system.interfaces.dto.*;
import cn.refinex.system.interfaces.vo.DrsInterfaceVO;
import cn.refinex.system.interfaces.vo.DrsVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据资源管理接口
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/drs")
@RequiredArgsConstructor
public class DataResourceController {

    private final DataResourceApplicationService dataResourceApplicationService;
    private final SystemApiAssembler systemApiAssembler;

    /**
     * 查询数据资源列表
     *
     * @param query 查询条件
     * @return 数据资源列表
     */
    @GetMapping
    public MultiResponse<DrsVO> listDrs(@Valid DrsListQuery query) {
        QueryDrsListCommand command = systemApiAssembler.toQueryDrsListCommand(query);
        List<DrsDTO> list = dataResourceApplicationService.listDrs(command);
        return MultiResponse.of(systemApiAssembler.toDrsVoList(list));
    }

    /**
     * 查询数据资源详情
     *
     * @param drsId 数据资源ID
     * @return 数据资源详情
     */
    @GetMapping("/{drsId}")
    public SingleResponse<DrsVO> getDrs(@PathVariable @Positive(message = "数据资源ID必须大于0") Long drsId) {
        DrsDTO dto = dataResourceApplicationService.getDrs(drsId);
        return SingleResponse.of(systemApiAssembler.toDrsVo(dto));
    }

    /**
     * 创建数据资源
     *
     * @param request 创建请求
     * @return 数据资源详情
     */
    @PostMapping
    public SingleResponse<DrsVO> createDrs(@Valid @RequestBody DrsCreateRequest request) {
        CreateDrsCommand command = systemApiAssembler.toCreateDrsCommand(request);
        DrsDTO dto = dataResourceApplicationService.createDrs(command);
        return SingleResponse.of(systemApiAssembler.toDrsVo(dto));
    }

    /**
     * 更新数据资源
     *
     * @param drsId   数据资源ID
     * @param request 更新请求
     * @return 数据资源详情
     */
    @PutMapping("/{drsId}")
    public SingleResponse<DrsVO> updateDrs(@PathVariable @Positive(message = "数据资源ID必须大于0") Long drsId,
                                           @Valid @RequestBody DrsUpdateRequest request) {
        UpdateDrsCommand command = systemApiAssembler.toUpdateDrsCommand(request);
        command.setDrsId(drsId);
        DrsDTO dto = dataResourceApplicationService.updateDrs(command);
        return SingleResponse.of(systemApiAssembler.toDrsVo(dto));
    }

    /**
     * 删除数据资源
     *
     * @param drsId 数据资源ID
     * @return 操作结果
     */
    @DeleteMapping("/{drsId}")
    public SingleResponse<Void> deleteDrs(@PathVariable @Positive(message = "数据资源ID必须大于0") Long drsId) {
        dataResourceApplicationService.deleteDrs(drsId);
        return SingleResponse.of(null);
    }

    /**
     * 查询数据资源接口列表
     *
     * @param query 查询条件
     * @return 数据资源接口列表
     */
    @GetMapping("/interfaces")
    public MultiResponse<DrsInterfaceVO> listDrsInterfaces(@Valid DrsInterfaceListQuery query) {
        QueryDrsInterfaceListCommand command = systemApiAssembler.toQueryDrsInterfaceListCommand(query);
        List<DrsInterfaceDTO> list = dataResourceApplicationService.listDrsInterfaces(command);
        return MultiResponse.of(systemApiAssembler.toDrsInterfaceVoList(list));
    }

    /**
     * 查询数据资源接口详情
     *
     * @param interfaceId 接口ID
     * @return 数据资源接口详情
     */
    @GetMapping("/interfaces/{interfaceId}")
    public SingleResponse<DrsInterfaceVO> getDrsInterface(@PathVariable @Positive(message = "接口ID必须大于0") Long interfaceId) {
        DrsInterfaceDTO dto = dataResourceApplicationService.getDrsInterface(interfaceId);
        return SingleResponse.of(systemApiAssembler.toDrsInterfaceVo(dto));
    }

    /**
     * 创建数据资源接口
     *
     * @param drsId   数据资源ID
     * @param request 创建请求
     * @return 数据资源接口详情
     */
    @PostMapping("/{drsId}/interfaces")
    public SingleResponse<DrsInterfaceVO> createDrsInterface(@PathVariable @Positive(message = "数据资源ID必须大于0") Long drsId,
                                                             @Valid @RequestBody DrsInterfaceCreateRequest request) {
        CreateDrsInterfaceCommand command = systemApiAssembler.toCreateDrsInterfaceCommand(request);
        command.setDrsId(drsId);
        DrsInterfaceDTO dto = dataResourceApplicationService.createDrsInterface(command);
        return SingleResponse.of(systemApiAssembler.toDrsInterfaceVo(dto));
    }

    /**
     * 更新数据资源接口
     *
     * @param interfaceId 接口ID
     * @param request     更新请求
     * @return 数据资源接口详情
     */
    @PutMapping("/interfaces/{interfaceId}")
    public SingleResponse<DrsInterfaceVO> updateDrsInterface(@PathVariable @Positive(message = "接口ID必须大于0") Long interfaceId,
                                                             @Valid @RequestBody DrsInterfaceUpdateRequest request) {
        UpdateDrsInterfaceCommand command = systemApiAssembler.toUpdateDrsInterfaceCommand(request);
        command.setInterfaceId(interfaceId);
        DrsInterfaceDTO dto = dataResourceApplicationService.updateDrsInterface(command);
        return SingleResponse.of(systemApiAssembler.toDrsInterfaceVo(dto));
    }

    /**
     * 删除数据资源接口
     *
     * @param interfaceId 接口ID
     * @return 操作结果
     */
    @DeleteMapping("/interfaces/{interfaceId}")
    public SingleResponse<Void> deleteDrsInterface(@PathVariable @Positive(message = "接口ID必须大于0") Long interfaceId) {
        dataResourceApplicationService.deleteDrsInterface(interfaceId);
        return SingleResponse.of(null);
    }
}
