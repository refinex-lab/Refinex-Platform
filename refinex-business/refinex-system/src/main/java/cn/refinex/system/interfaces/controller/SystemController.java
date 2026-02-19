package cn.refinex.system.interfaces.controller;

import cn.refinex.base.response.MultiResponse;
import cn.refinex.base.response.SingleResponse;
import cn.refinex.system.application.command.CreateSystemCommand;
import cn.refinex.system.application.command.QuerySystemListCommand;
import cn.refinex.system.application.command.UpdateSystemCommand;
import cn.refinex.system.application.dto.SystemDTO;
import cn.refinex.system.application.service.SystemApplicationService;
import cn.refinex.system.interfaces.assembler.SystemApiAssembler;
import cn.refinex.system.interfaces.dto.SystemCreateRequest;
import cn.refinex.system.interfaces.dto.SystemListQuery;
import cn.refinex.system.interfaces.dto.SystemUpdateRequest;
import cn.refinex.system.interfaces.vo.SystemVO;
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
 * 系统定义管理接口
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/systems")
@RequiredArgsConstructor
public class SystemController {

    private final SystemApplicationService systemApplicationService;
    private final SystemApiAssembler systemApiAssembler;

    /**
     * 查询系统列表
     *
     * @param query 查询条件
     * @return 系统列表
     */
    @GetMapping
    public MultiResponse<SystemVO> listSystems(@Valid SystemListQuery query) {
        QuerySystemListCommand command = systemApiAssembler.toQuerySystemListCommand(query);
        List<SystemDTO> systems = systemApplicationService.listSystems(command);
        return MultiResponse.of(systemApiAssembler.toSystemVoList(systems));
    }

    /**
     * 查询系统详情
     *
     * @param systemId 系统ID
     * @return 系统详情
     */
    @GetMapping("/{systemId}")
    public SingleResponse<SystemVO> getSystem(@PathVariable @Positive(message = "系统ID必须大于0") Long systemId) {
        SystemDTO system = systemApplicationService.getSystem(systemId);
        return SingleResponse.of(systemApiAssembler.toSystemVo(system));
    }

    /**
     * 创建系统
     *
     * @param request 创建请求
     * @return 系统详情
     */
    @PostMapping
    public SingleResponse<SystemVO> createSystem(@Valid @RequestBody SystemCreateRequest request) {
        CreateSystemCommand command = systemApiAssembler.toCreateSystemCommand(request);
        SystemDTO created = systemApplicationService.createSystem(command);
        return SingleResponse.of(systemApiAssembler.toSystemVo(created));
    }

    /**
     * 更新系统
     *
     * @param systemId 系统ID
     * @param request  更新请求
     * @return 系统详情
     */
    @PutMapping("/{systemId}")
    public SingleResponse<SystemVO> updateSystem(@PathVariable @Positive(message = "系统ID必须大于0") Long systemId,
                                                 @Valid @RequestBody SystemUpdateRequest request) {
        UpdateSystemCommand command = systemApiAssembler.toUpdateSystemCommand(request);
        command.setSystemId(systemId);
        SystemDTO updated = systemApplicationService.updateSystem(command);
        return SingleResponse.of(systemApiAssembler.toSystemVo(updated));
    }
}
