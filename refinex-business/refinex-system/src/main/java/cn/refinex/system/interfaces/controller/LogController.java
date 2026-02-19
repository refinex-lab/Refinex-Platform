package cn.refinex.system.interfaces.controller;

import cn.refinex.base.response.MultiResponse;
import cn.refinex.base.response.SingleResponse;
import cn.refinex.system.application.command.QueryErrorLogListCommand;
import cn.refinex.system.application.command.QueryLoginLogListCommand;
import cn.refinex.system.application.command.QueryNotifyLogListCommand;
import cn.refinex.system.application.command.QueryOperateLogListCommand;
import cn.refinex.system.application.dto.ErrorLogDTO;
import cn.refinex.system.application.dto.LoginLogDTO;
import cn.refinex.system.application.dto.NotifyLogDTO;
import cn.refinex.system.application.dto.OperateLogDTO;
import cn.refinex.system.application.service.LogApplicationService;
import cn.refinex.system.interfaces.assembler.SystemApiAssembler;
import cn.refinex.system.interfaces.dto.ErrorLogListQuery;
import cn.refinex.system.interfaces.dto.LoginLogListQuery;
import cn.refinex.system.interfaces.dto.NotifyLogListQuery;
import cn.refinex.system.interfaces.dto.OperateLogListQuery;
import cn.refinex.system.interfaces.vo.ErrorLogVO;
import cn.refinex.system.interfaces.vo.LoginLogVO;
import cn.refinex.system.interfaces.vo.NotifyLogVO;
import cn.refinex.system.interfaces.vo.OperateLogVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 日志查看接口
 *
 * @author refinex
 */
@Validated
@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogApplicationService logApplicationService;
    private final SystemApiAssembler systemApiAssembler;

    /**
     * 查询登录日志列表
     *
     * @param query 查询条件
     * @return 登录日志列表
     */
    @GetMapping("/login")
    public MultiResponse<LoginLogVO> listLoginLogs(@Valid LoginLogListQuery query) {
        QueryLoginLogListCommand command = systemApiAssembler.toQueryLoginLogListCommand(query);
        List<LoginLogDTO> list = logApplicationService.listLoginLogs(command);
        return MultiResponse.of(systemApiAssembler.toLoginLogVoList(list));
    }

    /**
     * 查询登录日志详情
     *
     * @param logId 日志ID
     * @return 登录日志详情
     */
    @GetMapping("/login/{logId}")
    public SingleResponse<LoginLogVO> getLoginLog(@PathVariable @Positive(message = "日志ID必须大于0") Long logId) {
        LoginLogDTO dto = logApplicationService.getLoginLog(logId);
        return SingleResponse.of(systemApiAssembler.toLoginLogVo(dto));
    }

    /**
     * 查询操作日志列表
     *
     * @param query 查询条件
     * @return 操作日志列表
     */
    @GetMapping("/operate")
    public MultiResponse<OperateLogVO> listOperateLogs(@Valid OperateLogListQuery query) {
        QueryOperateLogListCommand command = systemApiAssembler.toQueryOperateLogListCommand(query);
        List<OperateLogDTO> list = logApplicationService.listOperateLogs(command);
        return MultiResponse.of(systemApiAssembler.toOperateLogVoList(list));
    }

    /**
     * 查询操作日志详情
     *
     * @param logId 日志ID
     * @return 操作日志详情
     */
    @GetMapping("/operate/{logId}")
    public SingleResponse<OperateLogVO> getOperateLog(@PathVariable @Positive(message = "日志ID必须大于0") Long logId) {
        OperateLogDTO dto = logApplicationService.getOperateLog(logId);
        return SingleResponse.of(systemApiAssembler.toOperateLogVo(dto));
    }

    /**
     * 查询错误日志列表
     *
     * @param query 查询条件
     * @return 错误日志列表
     */
    @GetMapping("/error")
    public MultiResponse<ErrorLogVO> listErrorLogs(@Valid ErrorLogListQuery query) {
        QueryErrorLogListCommand command = systemApiAssembler.toQueryErrorLogListCommand(query);
        List<ErrorLogDTO> list = logApplicationService.listErrorLogs(command);
        return MultiResponse.of(systemApiAssembler.toErrorLogVoList(list));
    }

    /**
     * 查询错误日志详情
     *
     * @param logId 日志ID
     * @return 错误日志详情
     */
    @GetMapping("/error/{logId}")
    public SingleResponse<ErrorLogVO> getErrorLog(@PathVariable @Positive(message = "日志ID必须大于0") Long logId) {
        ErrorLogDTO dto = logApplicationService.getErrorLog(logId);
        return SingleResponse.of(systemApiAssembler.toErrorLogVo(dto));
    }

    /**
     * 查询通知日志列表
     *
     * @param query 查询条件
     * @return 通知日志列表
     */
    @GetMapping("/notify")
    public MultiResponse<NotifyLogVO> listNotifyLogs(@Valid NotifyLogListQuery query) {
        QueryNotifyLogListCommand command = systemApiAssembler.toQueryNotifyLogListCommand(query);
        List<NotifyLogDTO> list = logApplicationService.listNotifyLogs(command);
        return MultiResponse.of(systemApiAssembler.toNotifyLogVoList(list));
    }

    /**
     * 查询通知日志详情
     *
     * @param logId 日志ID
     * @return 通知日志详情
     */
    @GetMapping("/notify/{logId}")
    public SingleResponse<NotifyLogVO> getNotifyLog(@PathVariable @Positive(message = "日志ID必须大于0") Long logId) {
        NotifyLogDTO dto = logApplicationService.getNotifyLog(logId);
        return SingleResponse.of(systemApiAssembler.toNotifyLogVo(dto));
    }
}
