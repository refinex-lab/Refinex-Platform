package cn.refinex.system.application.service;

import cn.refinex.base.exception.BizException;
import cn.refinex.base.response.PageResponse;
import cn.refinex.base.utils.PageUtils;
import cn.refinex.system.application.assembler.SystemDomainAssembler;
import cn.refinex.system.application.command.QueryErrorLogListCommand;
import cn.refinex.system.application.command.QueryLoginLogListCommand;
import cn.refinex.system.application.command.QueryNotifyLogListCommand;
import cn.refinex.system.application.command.QueryOperateLogListCommand;
import cn.refinex.system.application.dto.ErrorLogDTO;
import cn.refinex.system.application.dto.LoginLogDTO;
import cn.refinex.system.application.dto.NotifyLogDTO;
import cn.refinex.system.application.dto.OperateLogDTO;
import cn.refinex.system.domain.error.SystemErrorCode;
import cn.refinex.system.domain.model.entity.ErrorLogEntity;
import cn.refinex.system.domain.model.entity.LoginLogEntity;
import cn.refinex.system.domain.model.entity.NotifyLogEntity;
import cn.refinex.system.domain.model.entity.OperateLogEntity;
import cn.refinex.system.domain.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 日志查询应用服务
 *
 * @author refinex
 */
@Service
@RequiredArgsConstructor
public class LogApplicationService {

    private final LogRepository logRepository;
    private final SystemDomainAssembler systemDomainAssembler;

    /**
     * 查询登录日志列表
     *
     * @param command 查询命令
     * @return 登录日志列表
     */
    public PageResponse<LoginLogDTO> listLoginLogs(QueryLoginLogListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(), PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);

        PageResponse<LoginLogEntity> entities = logRepository.listLoginLogs(
                command == null ? null : command.getUserId(),
                command == null ? null : command.getEstabId(),
                command == null ? null : command.getSuccess(),
                command == null ? null : command.getLoginType(),
                command == null ? null : command.getSourceType(),
                command == null ? null : command.getStartTime(),
                command == null ? null : command.getEndTime(),
                currentPage,
                pageSize
        );

        List<LoginLogEntity> entitiesData = entities.getData();
        if (CollectionUtils.isEmpty(entitiesData)) {
            return PageResponse.of(Collections.emptyList(), 0, pageSize, currentPage);
        }

        List<LoginLogDTO> result = new ArrayList<>();
        for (LoginLogEntity entity : entities.getData()) {
            result.add(systemDomainAssembler.toLoginLogDto(entity));
        }

        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询登录日志
     *
     * @param logId 日志ID
     * @return 登录日志
     */
    public LoginLogDTO getLoginLog(Long logId) {
        LoginLogEntity entity = logRepository.findLoginLogById(logId);
        if (entity == null) {
            throw new BizException(SystemErrorCode.LOGIN_LOG_NOT_FOUND);
        }
        return systemDomainAssembler.toLoginLogDto(entity);
    }

    /**
     * 查询操作日志列表
     *
     * @param command 查询命令
     * @return 操作日志列表
     */
    public PageResponse<OperateLogDTO> listOperateLogs(QueryOperateLogListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);
        PageResponse<OperateLogEntity> entities = logRepository.listOperateLogs(
                command == null ? null : command.getUserId(),
                command == null ? null : command.getEstabId(),
                command == null ? null : command.getSuccess(),
                command == null ? null : command.getModuleCode(),
                command == null ? null : command.getRequestPath(),
                command == null ? null : command.getStartTime(),
                command == null ? null : command.getEndTime(),
                currentPage,
                pageSize
        );
        List<OperateLogDTO> result = new ArrayList<>();
        for (OperateLogEntity entity : entities.getData()) {
            result.add(systemDomainAssembler.toOperateLogDto(entity));
        }
        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询操作日志
     *
     * @param logId 日志ID
     * @return 操作日志
     */
    public OperateLogDTO getOperateLog(Long logId) {
        OperateLogEntity entity = logRepository.findOperateLogById(logId);
        if (entity == null) {
            throw new BizException(SystemErrorCode.OPERATE_LOG_NOT_FOUND);
        }
        return systemDomainAssembler.toOperateLogDto(entity);
    }

    /**
     * 查询错误日志列表
     *
     * @param command 查询命令
     * @return 错误日志列表
     */
    public PageResponse<ErrorLogDTO> listErrorLogs(QueryErrorLogListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);
        PageResponse<ErrorLogEntity> entities = logRepository.listErrorLogs(
                command == null ? null : command.getServiceName(),
                command == null ? null : command.getErrorLevel(),
                command == null ? null : command.getRequestPath(),
                command == null ? null : command.getStartTime(),
                command == null ? null : command.getEndTime(),
                currentPage,
                pageSize
        );
        List<ErrorLogDTO> result = new ArrayList<>();
        for (ErrorLogEntity entity : entities.getData()) {
            result.add(systemDomainAssembler.toErrorLogDto(entity));
        }
        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询错误日志
     *
     * @param logId 日志ID
     * @return 错误日志
     */
    public ErrorLogDTO getErrorLog(Long logId) {
        ErrorLogEntity entity = logRepository.findErrorLogById(logId);
        if (entity == null) {
            throw new BizException(SystemErrorCode.ERROR_LOG_NOT_FOUND);
        }
        return systemDomainAssembler.toErrorLogDto(entity);
    }

    /**
     * 查询通知日志列表
     *
     * @param command 查询命令
     * @return 通知日志列表
     */
    public PageResponse<NotifyLogDTO> listNotifyLogs(QueryNotifyLogListCommand command) {
        int currentPage = PageUtils.normalizeCurrentPage(command == null ? null : command.getCurrentPage());
        int pageSize = PageUtils.normalizePageSize(command == null ? null : command.getPageSize(),
                PageUtils.DEFAULT_PAGE_SIZE, PageUtils.DEFAULT_MAX_PAGE_SIZE);
        PageResponse<NotifyLogEntity> entities = logRepository.listNotifyLogs(
                command == null ? null : command.getChannelType(),
                command == null ? null : command.getSceneCode(),
                command == null ? null : command.getReceiver(),
                command == null ? null : command.getSendStatus(),
                command == null ? null : command.getStartTime(),
                command == null ? null : command.getEndTime(),
                currentPage,
                pageSize
        );
        List<NotifyLogDTO> result = new ArrayList<>();
        for (NotifyLogEntity entity : entities.getData()) {
            result.add(systemDomainAssembler.toNotifyLogDto(entity));
        }
        return PageResponse.of(result, entities.getTotal(), entities.getPageSize(), entities.getCurrentPage());
    }

    /**
     * 查询通知日志
     *
     * @param logId 日志ID
     * @return 通知日志
     */
    public NotifyLogDTO getNotifyLog(Long logId) {
        NotifyLogEntity entity = logRepository.findNotifyLogById(logId);
        if (entity == null) {
            throw new BizException(SystemErrorCode.NOTIFY_LOG_NOT_FOUND);
        }
        return systemDomainAssembler.toNotifyLogDto(entity);
    }

}
