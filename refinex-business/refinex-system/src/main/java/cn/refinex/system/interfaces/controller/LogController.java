package cn.refinex.system.interfaces.controller;

import cn.refinex.base.response.PageResponse;
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
import cn.refinex.web.vo.PageResult;
import cn.refinex.web.vo.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public PageResult<LoginLogVO> listLoginLogs(@Valid LoginLogListQuery query) {
        QueryLoginLogListCommand command = systemApiAssembler.toQueryLoginLogListCommand(query);
        PageResponse<LoginLogDTO> list = logApplicationService.listLoginLogs(command);
        return PageResult.success(
                systemApiAssembler.toLoginLogVoList(list.getData()),
                list.getTotal(),
                list.getCurrentPage(),
                list.getPageSize()
        );
    }

    /**
     * 查询登录日志详情
     *
     * @param logId 日志ID
     * @return 登录日志详情
     */
    @GetMapping("/login/{logId}")
    public Result<LoginLogVO> getLoginLog(@PathVariable @Positive(message = "日志ID必须大于0") Long logId) {
        LoginLogDTO dto = logApplicationService.getLoginLog(logId);
        return Result.success(systemApiAssembler.toLoginLogVo(dto));
    }

    /**
     * 查询操作日志列表
     *
     * @param query 查询条件
     * @return 操作日志列表
     */
    @GetMapping("/operate")
    public PageResult<OperateLogVO> listOperateLogs(@Valid OperateLogListQuery query) {
        QueryOperateLogListCommand command = systemApiAssembler.toQueryOperateLogListCommand(query);
        PageResponse<OperateLogDTO> list = logApplicationService.listOperateLogs(command);
        return PageResult.success(
                systemApiAssembler.toOperateLogVoList(list.getData()),
                list.getTotal(),
                list.getCurrentPage(),
                list.getPageSize()
        );
    }

    /**
     * 查询操作日志详情
     *
     * @param logId 日志ID
     * @return 操作日志详情
     */
    @GetMapping("/operate/{logId}")
    public Result<OperateLogVO> getOperateLog(@PathVariable @Positive(message = "日志ID必须大于0") Long logId) {
        OperateLogDTO dto = logApplicationService.getOperateLog(logId);
        return Result.success(systemApiAssembler.toOperateLogVo(dto));
    }

    /**
     * 查询错误日志列表
     *
     * @param query 查询条件
     * @return 错误日志列表
     */
    @GetMapping("/error")
    public PageResult<ErrorLogVO> listErrorLogs(@Valid ErrorLogListQuery query) {
        QueryErrorLogListCommand command = systemApiAssembler.toQueryErrorLogListCommand(query);
        PageResponse<ErrorLogDTO> list = logApplicationService.listErrorLogs(command);
        return PageResult.success(
                systemApiAssembler.toErrorLogVoList(list.getData()),
                list.getTotal(),
                list.getCurrentPage(),
                list.getPageSize()
        );
    }

    /**
     * 查询错误日志详情
     *
     * @param logId 日志ID
     * @return 错误日志详情
     */
    @GetMapping("/error/{logId}")
    public Result<ErrorLogVO> getErrorLog(@PathVariable @Positive(message = "日志ID必须大于0") Long logId) {
        ErrorLogDTO dto = logApplicationService.getErrorLog(logId);
        return Result.success(systemApiAssembler.toErrorLogVo(dto));
    }

    /**
     * 查询通知日志列表
     *
     * @param query 查询条件
     * @return 通知日志列表
     */
    @GetMapping("/notify")
    public PageResult<NotifyLogVO> listNotifyLogs(@Valid NotifyLogListQuery query) {
        QueryNotifyLogListCommand command = systemApiAssembler.toQueryNotifyLogListCommand(query);
        PageResponse<NotifyLogDTO> list = logApplicationService.listNotifyLogs(command);
        return PageResult.success(
                systemApiAssembler.toNotifyLogVoList(list.getData()),
                list.getTotal(),
                list.getCurrentPage(),
                list.getPageSize()
        );
    }

    /**
     * 查询通知日志详情
     *
     * @param logId 日志ID
     * @return 通知日志详情
     */
    @GetMapping("/notify/{logId}")
    public Result<NotifyLogVO> getNotifyLog(@PathVariable @Positive(message = "日志ID必须大于0") Long logId) {
        NotifyLogDTO dto = logApplicationService.getNotifyLog(logId);
        return Result.success(systemApiAssembler.toNotifyLogVo(dto));
    }
}
